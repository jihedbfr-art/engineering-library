# Python Concurrency — Deep Dive

[python.md](python.md) covers the language overview. This page is about the single most-misunderstood topic in practical Python: why "just add threading" so often doesn't speed anything up, and what actually does — a genuinely different set of answers depending on whether your bottleneck is CPU or I/O, which is exactly the question most confusion here traces back to skipping.

## The GIL — the constraint that shapes everything else on this page

```
The Global Interpreter Lock (GIL): only ONE thread can execute Python
bytecode at any given instant, in the standard CPython interpreter —
regardless of how many CPU cores the machine actually has.
```
This single fact is the reason `threading` in Python behaves so differently from `threading` in Java or Go, and it's worth being precise about exactly what it does and doesn't prevent:

```python
# CPU-bound work — threading does NOT help, because of the GIL
import threading, time

def cpu_heavy():
    total = 0
    for i in range(50_000_000):
        total += i

start = time.time()
threads = [threading.Thread(target=cpu_heavy) for _ in range(4)]
[t.start() for t in threads]
[t.join() for t in threads]
print(time.time() - start)
# On a genuine multi-core machine, this takes roughly the SAME time as running
# cpu_heavy() four times sequentially — the GIL serializes actual execution,
# so four threads of pure CPU work buy you essentially nothing here
```
The GIL is released during I/O waits (network calls, file reads, `time.sleep`) — which is exactly why threading *does* genuinely help for I/O-bound work, even though it does essentially nothing for CPU-bound work. **Correctly diagnosing which category your actual bottleneck falls into is the single most important decision on this entire page** — everything below is really just "what do you reach for, once you actually know the answer."

## I/O-bound → `asyncio` (the modern default) or `threading`

```python
import asyncio, aiohttp

async def fetch(session, url):
    async with session.get(url) as response:
        return await response.text()

async def fetch_all(urls):
    async with aiohttp.ClientSession() as session:
        tasks = [fetch(session, url) for url in urls]
        return await asyncio.gather(*tasks)   # all requests genuinely run concurrently

results = asyncio.run(fetch_all(["https://api1.example.com", "https://api2.example.com"]))
```
`asyncio` runs a **single-threaded event loop** that switches between tasks specifically at `await` points, whenever one task is actually waiting on I/O rather than burning CPU — conceptually the same cooperative-scheduling idea as [Node.js's event loop](../backend/nodejs/nodejs-backend.md), and worth recognizing as the same underlying pattern if you've worked with Node before. Fetching 100 URLs concurrently with `asyncio` can be dramatically faster than fetching them one at a time sequentially, precisely because the CPU sits idle waiting on network I/O either way — `asyncio` is what actually keeps it doing something useful (starting the next request) during exactly that idle wait, instead of wasting it.

## The async "colored function" problem — a real, practical annoyance worth knowing about upfront

```python
async def fetch_data():           # an "async function" — a genuinely different kind of function
    return await some_io_call()

def process(data):                 # a normal, "sync" function
    return fetch_data()             # WRONG — this returns an unawaited coroutine object,
                                     # not the actual result; a classic, very common beginner bug
```
Once a function is `async`, essentially everything that calls it also needs to become `async`, propagating outward through the call stack — often called the "colored function" problem, because sync and async functions can't freely, casually call each other the way two ordinary sync functions can. This is a real, structural property of Python's (and JavaScript's) async model, not a Python-specific bug — worth knowing upfront specifically so the friction of "why can't I just call this from anywhere" doesn't feel like a personal mistake the first time you hit it.

## CPU-bound → `multiprocessing` (the actual, correct fix)

```python
from multiprocessing import Pool

def cpu_heavy(n):
    total = 0
    for i in range(n):
        total += i
    return total

if __name__ == "__main__":
    with Pool(processes=4) as pool:
        results = pool.map(cpu_heavy, [50_000_000] * 4)
    # Each process has its OWN Python interpreter, its OWN GIL —
    # genuine parallel execution across real CPU cores, unlike threading above
```
`multiprocessing` sidesteps the GIL entirely, correctly, by giving each worker its own separate OS process, each with its own independent interpreter and GIL — real, actual parallelism across cores, at the cost of real overhead: process startup is measurably heavier than thread startup, and sharing data between processes requires explicit, deliberate serialization (pickling) rather than simply sharing memory the way threads naturally can. This overhead is exactly why `multiprocessing` is reached for specifically when the work is genuinely CPU-bound and the computation per task is substantial enough to actually be worth that overhead — not as a reflexive default for every concurrency need.

## The decision, stated as a simple, genuinely useful table

| Workload | Reach for | Why |
|---|---|---|
| Many slow network/API calls | `asyncio` | Single-threaded, extremely low overhead per concurrent task, GIL isn't the bottleneck here at all |
| Many slow network calls, simpler mental model preferred | `threading` | GIL releases during I/O waits — genuinely works, if less efficient at very large scale than asyncio |
| Heavy computation (image processing, number crunching) | `multiprocessing` | The ONLY one of these three that achieves genuine, real parallelism across CPU cores |
| Heavy computation, need it fast and don't want the overhead | Consider `numpy`/`C extensions` | These release the GIL internally during their own compiled, native execution — often the actual best answer, and easy to miss when only thinking in terms of the three options above |

## Why the GIL still exists in 2026 (a brief, honest note)

Removing the GIL entirely would meaningfully speed up multi-threaded CPU-bound Python — and would also risk breaking real single-threaded performance and the safety guarantees a huge, existing ecosystem of C extensions currently depends on. **PEP 703** introduced an officially supported, opt-in build of CPython without the GIL, landing incrementally starting around Python 3.13 — a genuinely major, actively-in-progress change to how Python concurrency works, worth being aware exists and is underway, rather than assuming the GIL is a permanent, unchangeable fact of the language forever.

## Where this connects

The GIL's core lesson — know precisely which resource you're actually contending for before reaching for a concurrency tool — is the exact same discipline as correctly diagnosing [database bottlenecks](../databases/transactions-concurrency.md) or [capacity-planning load test results](../sre/capacity-planning.md) before reaching for a fix: identify the real, specific constraint first, then apply the specific tool that actually addresses that constraint, not a generic "add more concurrency" reflex applied blindly regardless of what's actually limiting you.
