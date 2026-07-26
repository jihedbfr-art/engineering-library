---
name: load-testing-plan-writer
description: Write a load-testing plan for a service or endpoint before a launch, migration, or expected traffic spike — target numbers, ramp shape, what to measure, and the abort conditions. Use when asked to "plan a load test", "make sure this can handle Black Friday traffic", or before any change that changes a system's expected concurrency or throughput.
---

# Load-testing plan writer

A load test without a plan produces a number nobody can interpret — "it handled 500 req/s" means
nothing without knowing what 500 req/s represents (peak? average? 10x headroom?) and what broke
first when it stopped handling more. This skill turns "we should load test this" into a plan
someone can actually execute and someone else can actually judge the results of.

## What the plan needs, and why each part earns its place

**Target load, derived from a real number, not a round one.** Where does the number come from —
current peak traffic times a safety multiplier, a specific projected event (launch, marketing
campaign, seasonal spike), or a contractual SLA? "Test up to 1000 req/s" without a source is a
number someone picked because it sounded thorough, not because it maps to anything real.

**Ramp shape, not just a peak.** A sudden step to peak load tests something different from a
gradual ramp — real traffic (organic growth, a marketing push) usually ramps, while a real failure
mode (a retry storm, a cache stampede after an eviction) is a step. Both are worth testing if
either is plausible for this system; picking only one and calling it "the load test" tests one
scenario and calls it comprehensive.

**What actually gets measured, beyond req/s and average latency.** p95/p99 latency (averages hide
the tail that actually generates complaints), error rate by type (timeouts vs. 500s vs. connection
refused mean different bottlenecks), resource saturation on every layer in the request path (app
CPU/memory, DB connections, cache hit rate, downstream API rate limits) — not just the layer being
tested, because the load test's job is finding which layer breaks *first*, and that's rarely the
one everyone assumed.

**Abort conditions, decided before the test runs, not during.** At what error rate or latency does
the test stop rather than continue pushing past a real failure into cascading damage (especially
relevant for a test against anything shared with real traffic, like a shared database or a
third-party API with its own rate limits)? Deciding this mid-test under pressure is how a load test
becomes an actual incident.

**Environment fidelity, stated honestly.** Is this against production-scale infrastructure, a
staging environment at some fraction of prod capacity, or a synthetic isolated environment? Results
from a staging environment at 20% of production's database instance size don't scale linearly —
say so in the plan instead of letting the final report imply a false confidence.

## Structure to produce

```
## Goal
What decision does this test inform? ("Can we handle 3x current peak before Black Friday" is a
goal; "load test the checkout service" is a task description, not a goal.)

## Target load
Number, source of the number, ramp shape (step vs. gradual, and why).

## What to measure
Per-layer: app, cache, database, downstream dependencies. p95/p99, not just average.

## Abort conditions
Specific thresholds, decided now.

## Environment
What's actually being tested against, and how that limits interpreting the results.

## Pass/fail criteria
Stated before the test runs, not fitted to whatever number comes out.
```

## What NOT to do

- Don't write a plan that only tests the happy path — include at least one failure-injection
  scenario (a downstream dependency going slow or unavailable mid-test) if the system has any
  external dependency at all, since "handles load" and "handles load when one dependency degrades"
  are different guarantees.
- Don't let the plan imply a single load test proves the system is "ready" — one run under one set
  of conditions is one data point, say that plainly rather than overselling the conclusion.
