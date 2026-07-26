---
name: cost-optimization-reviewer
description: Review infrastructure or cloud spend for real waste — over-provisioned resources, orphaned/idle resources still billing, and inefficient architecture choices — ranked by savings versus effort. Use when asked to "review our cloud costs", "find waste in this infrastructure", or when a spend number needs explaining before a budget conversation.
---

# Cost optimization reviewer

Cost reviews tend to produce either a wall of small suggestions nobody prioritizes, or one big
scary number with no path to acting on it. This skill produces a short, ranked list instead —
concrete findings, each with an estimated saving and an estimated effort, so the reader can
actually decide what to do first.

## Where waste actually hides, most to least common

1. **Idle or orphaned resources still billing.** Unattached storage volumes from terminated
   instances, load balancers with no healthy targets behind them, snapshots kept indefinitely with
   no retention policy, dev/staging environments left running outside working hours. This category
   is usually the fastest to fix (delete or schedule shutdown) and the easiest to justify — there's
   no tradeoff being accepted, just genuine waste.
2. **Over-provisioned compute for actual utilization.** An instance sized for a peak that happens
   twice a year, running at that size year-round, is the second most common finding. Look at actual
   CPU/memory utilization over a real time window (not a single snapshot) before recommending a
   downsize — a resource that looks idle at 2pm might be the one thing saving the system at
   month-end batch processing.
3. **Storage tier mismatch.** Data accessed daily sitting in an archival tier costs retrieval fees
   that add up; data untouched for months sitting in a hot tier costs the opposite way. Check access
   patterns against the storage class actually in use, not just the raw storage cost.
4. **Data transfer costs from an avoidable architecture choice.** Cross-region or cross-AZ traffic
   that could be same-region, a CDN misconfigured to fetch from origin more often than necessary, an
   API gateway routing that adds an unnecessary hop — these are often invisible on a resource-level
   bill but show up clearly once transfer costs are broken out by path.
5. **Reserved/committed-use coverage gaps.** Steady-state, predictable workloads still paying
   on-demand rates because nobody revisited commitment purchases since the infrastructure grew —
   this is pure "we're leaving a known discount on the table," not an architecture change.
6. **A model/API cost problem hiding in an infra review.** If any of the spend is LLM API calls,
   this overlaps directly with [`10-model-routing-and-cost`](../../10-model-routing-and-cost/) in
   this library — check whether routing, caching, or tier selection is actually being applied
   before treating it as a pure infrastructure cost problem.

## Output shape

```
| Finding | Estimated monthly saving | Effort | Risk of the fix |
|---|---|---|---|
```

Ranked by saving-to-effort ratio, not raw saving size — a $200/month fix that takes an hour usually
belongs above a $2000/month fix that needs a quarter-long re-architecture, even though the second
number is bigger, because the first one actually gets done.

## What NOT to do

- Don't recommend downsizing or deleting anything without checking utilization over a real window
  first — a "waste" finding that turns out to be genuinely needed capacity, acted on without
  checking, causes an outage and destroys the credibility of every other finding in the review.
- Don't present savings estimates as precise numbers pulled from nowhere — state the assumption
  behind each estimate (current on-demand rate, observed utilization window) so the reader can
  sanity-check it against their own bill.
