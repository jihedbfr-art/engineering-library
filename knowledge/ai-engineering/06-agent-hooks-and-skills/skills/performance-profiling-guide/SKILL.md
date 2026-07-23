---
name: performance-profiling-guide
description: Diagnose a performance problem methodically — measure before guessing, find the actual bottleneck, propose a fix scoped to what was measured. Use when asked to "make this faster", "this is slow", or "profile this code", especially when the instinct is to optimize before measuring.
---

# Performance profiling guide

The single most common performance-tuning mistake: optimizing the part of the code that looks
slow instead of the part that measurably is. This skill enforces the order: measure, find the
actual bottleneck, then and only then propose a change — never the reverse.

## Process

1. **Establish what "slow" means, concretely**, before touching anything. A vague "this is slow"
   needs a number: current latency/throughput, expected/acceptable latency, and under what load
   (one request, or a specific concurrent volume). Without a target, "faster" has no stopping
   point.

2. **Measure before hypothesizing.** If profiling data (a flame graph, timing logs, a database
   query plan) is available, use it to find the actual hot path — resist the pull toward the
   piece of code that *looks* complex or inefficient; the real bottleneck is very often somewhere
   unglamorous (a query missing an index, a synchronous call with no timeout, N+1 queries) rather
   than the algorithmically interesting part of the code.

3. **If no profiling data exists, say so and ask for it or suggest how to gather it**
   (`EXPLAIN ANALYZE` for a slow query, a flame graph tool for the language/runtime in use, timing
   instrumentation around suspected sections) — proposing an optimization without measurement
   data is a guess, not a diagnosis, and should be labeled as a guess if that's genuinely all
   that's available.

4. **Once the bottleneck is identified, scope the fix to it specifically.** A confirmed N+1 query
   gets a fetch-join or batching fix — it does not justify a broader "let's also cache
   everything and switch database drivers" expansion unless those are independently measured
   problems too.

5. **State the expected improvement and how to verify it** — re-measure the same metric from
   step 1 after the fix, not a different metric that happens to look better.

## Common root causes, roughly in order of how often they turn out to be the real answer

- N+1 database queries (see engineering-failures-style write-ups on this pattern — it's the most
  common "mysterious slowness" root cause in database-backed applications)
- Missing or unused index on a frequently-filtered/joined column
- Synchronous calls to a slow dependency with no caching or timeout
- Unbounded result sets loaded fully into memory instead of paginated/streamed
- Serialization/deserialization overhead on a hot path (especially with reflection-heavy
  serializers)
- Lock contention under concurrency (looks fine in single-request testing, degrades under load)

## What NOT to do

- Don't recommend a rewrite in a different language/framework as a first response to a
  performance problem — that's almost always disproportionate to the actual bottleneck, and
  skips the measurement step entirely.
- Don't optimize code that isn't on the measured hot path, even if it looks inefficient — time
  spent there doesn't move the metric that actually matters.
- Don't declare victory without re-measuring — "this should be faster now" is a hypothesis, not
  a result.
