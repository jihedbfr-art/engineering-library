# JavaScript Event Loop — Deep Dive

[javascript-typescript.md](javascript-typescript.md) covers the language overview. This page is about the single mechanism that explains almost every "why did this run in that order" surprise in JavaScript — and it applies identically whether you're in a browser or [Node.js](../backend/nodejs/nodejs-backend.md).

## The single-threaded reality, and how it still handles concurrency

```
JavaScript runs on ONE thread. Full stop. No true parallel execution of
your JS code, ever — unlike Java's threads or Go's goroutines.

So how does it handle thousands of concurrent network requests without
blocking? By NEVER blocking on I/O in the first place — I/O is handed
off to the runtime (the browser's Web APIs, or Node's libuv), and your
JS code is only invoked again once that I/O actually completes.
```
This is the entire trick: JavaScript doesn't achieve concurrency by running multiple things *at once* — it achieves it by never sitting idle waiting on anything, handing off I/O to the underlying platform and getting called back later. It's cooperative, callback-driven concurrency on a single thread, not true parallelism.

## Call stack, Web APIs/libuv, callback queue, microtask queue — the four pieces

```
┌─────────────────┐
│   Call Stack      │  ← your currently-executing JS code, synchronous, LIFO
└─────────────────┘
         │  hands off async work (setTimeout, fetch, fs.readFile...)
         ▼
┌─────────────────┐
│ Web APIs / libuv   │  ← the runtime actually performs the I/O, OUTSIDE your JS thread
└─────────────────┘
         │  when done, queues the callback
         ▼
┌─────────────────┐        ┌─────────────────┐
│ Macrotask queue    │      │ Microtask queue    │  ← Promises, queueMicrotask
│ (setTimeout, I/O,   │      │ ALWAYS drained       │
│  UI events)          │      │ COMPLETELY before      │
└─────────────────┘        │ the next macrotask     │
                              └─────────────────┘
```
The **event loop** is simply: run everything currently on the call stack to completion, then check — is there a microtask waiting? Run *all* of them, completely, before doing anything else. Only once the microtask queue is fully empty does the loop pick up the next single macrotask.

## Why this exact ordering surprises almost everyone the first time

```javascript
console.log("1");
setTimeout(() => console.log("2"), 0);      // macrotask — even at 0ms delay
Promise.resolve().then(() => console.log("3"));  // microtask
console.log("4");

// Actual output: 1, 4, 3, 2  — NOT 1, 2, 3, 4 as the source order might suggest
```
`setTimeout(..., 0)` does **not** mean "run immediately" — it means "queue this as a macrotask, to run only after the current call stack finishes AND the microtask queue is fully drained." The Promise callback, being a microtask, always wins that race against a `setTimeout`, at literally any delay value including zero — this single ordering rule explains a genuinely large share of "why did my async code run in an order I didn't expect" confusion, and it's worth internalizing precisely rather than half-remembering.

## `async`/`await` — syntax sugar over exactly the same mechanism

```javascript
async function fetchUser(id) {
    console.log("start");
    const response = await fetch(`/api/users/${id}`);   // yields control here — NOT a real thread block
    console.log("got response");
    return response.json();
}
```
`await` doesn't block the thread the way a blocking call would in Java or Python — it suspends the `async` function at that exact point, immediately yields control back to the event loop so other code can run, and resumes the function (as a microtask) once the awaited promise actually settles. This is precisely why a slow `await` in one function never freezes an entire Node.js server — every other request being handled concurrently keeps running on that same single thread, interleaved, while this one specific function is paused waiting.

## The trap: accidentally blocking the one thread you have

```javascript
// This SYNCHRONOUS, CPU-heavy loop blocks EVERYTHING — every other request,
// every timer, every pending promise callback — for its entire duration:
function computeExpensiveThing() {
    let result = 0;
    for (let i = 0; i < 10_000_000_000; i++) { result += i; }   // no await anywhere — nothing to yield on
    return result;
}
```
Because there's only one thread, genuinely CPU-heavy synchronous work has nowhere to hide — it blocks the entire event loop, freezing every other concurrent request a Node.js server might be handling, for the whole duration. This is the JavaScript-specific instance of the same lesson [Python's GIL page](python-concurrency.md) teaches from a different angle: correctly diagnosing whether your actual bottleneck is I/O-bound or CPU-bound determines which tool is even the right one to reach for — `async`/`await` and the event loop genuinely solve I/O-bound concurrency beautifully and solve exactly nothing for CPU-bound work, which instead needs Node's `worker_threads` (a real, separate thread, deliberately outside the main event loop) or offloading to another process/service entirely.

## Promise combinators — the ordering/failure-handling toolbox

```javascript
Promise.all([fetchA(), fetchB(), fetchC()]);
// resolves when ALL succeed; rejects IMMEDIATELY if even one fails — the others' results are simply discarded

Promise.allSettled([fetchA(), fetchB(), fetchC()]);
// waits for ALL to finish regardless of outcome, returns each one's individual status —
// genuinely the right choice when partial failure is an acceptable, expected outcome

Promise.race([fetchA(), fetchB()]);
// resolves/rejects as soon as the FIRST one settles, whichever that is — useful for timeouts:
Promise.race([fetchData(), timeout(5000)]);
```
Picking the wrong combinator for the actual situation is a genuinely common, real bug source — `Promise.all` silently discarding two perfectly good results because a third, unrelated call failed is rarely the behavior anyone actually wanted when they reached for it without considering `allSettled` instead.

## Where this connects

This is the exact same "never block, hand off I/O, get called back" philosophy as [Node.js backend design](../backend/nodejs/nodejs-backend.md) — that page's "never block the event loop" rule *is* this page, applied specifically to backend service design. It's also a genuinely useful contrast against [Java's virtual threads](java-concurrency.md), which solve a closely related "don't waste resources on blocked work" problem, but via an entirely different mechanism — real, cheap, JVM-managed lightweight threads instead of a single-threaded cooperative event loop. Recognizing that these are two different, legitimate answers to a related underlying problem is worth more than memorizing either one in isolation.
