---
name: feature-flag-rollout-planner
description: Plan a staged feature-flag rollout with real percentage steps, what to watch at each step, and explicit rollback triggers. Use when asked to "plan the rollout for this feature", "how should we ship this behind a flag", or before flipping a flag on anything with meaningful blast radius (payments, auth, anything touching data migrations).
---

# Feature-flag rollout planner

"Ship it behind a flag" is not a plan by itself — a flag with no staged percentages, no defined
signals to watch, and no rollback trigger is just a slower version of shipping it to everyone at
once, with extra config. This skill turns a flag into an actual staged rollout.

## The stages, and what changes between them

A rollout isn't one flip from 0% to 100% — it's a sequence of deliberately small steps, each one
answering "did the last step reveal a problem" before taking the next:

1. **Internal only** (employees, a specific test account, a synthetic canary). Catches integration
   bugs a staging environment didn't — real production data shapes, real production load
   characteristics on shared infrastructure, real third-party API behavior.
2. **Small percentage of real users (1-5%).** The first exposure to genuinely unpredictable user
   behavior. This step's job is catching the failure mode nobody anticipated, so the blast radius
   has to be small enough that catching it late is still cheap.
3. **Meaningful percentage (25-50%).** By now correctness bugs should be caught; this step is about
   scale — does the feature hold up under real concurrent load, does a resource that seemed fine at
   5% start showing contention at 40%.
4. **Full rollout.** The step everyone remembers; it should be the least eventful one if the earlier
   stages did their job.

The exact percentages matter less than the principle: each step's *purpose* should be different
from the last (correctness → unpredictable behavior → scale → confirmation), not just "more
users." A plan that jumps 5% → 100% because "5% looked fine" skipped the step that would have
caught a scale-dependent problem.

## What to watch at each step — decided before, not during

For each stage, the plan needs explicit signals, not "monitor closely": specific error rate
thresholds, specific latency percentiles, a specific business metric if the feature could plausibly
move one (conversion rate, checkout completion). "Watch dashboards" is not a signal a rollback
decision can be made against under pressure — a number with a threshold is.

## Rollback triggers, stated as thresholds, not judgment calls

"If something looks wrong, roll back" sounds cautious but is actually the worse plan — under
pressure, with partial information, "does this look wrong" gets litigated in an incident channel
while the flag stays on. A rollback trigger stated as "error rate on this endpoint exceeds 2% for
5 consecutive minutes" is something anyone on the team can act on immediately, without needing the
original author present to interpret it.

## Structure to produce

```
## Feature and blast radius
What this touches, and the worst plausible outcome if it's broken in production.

## Stages
Percentage, duration at that percentage, and what this specific stage is meant to catch.

## Signals per stage
Concrete thresholds — error rate, latency, business metric — not "monitor closely."

## Rollback triggers
Stated as thresholds anyone can act on, plus who has authority to pull the trigger.

## Data/schema considerations
If this flag gates a data migration or schema change, is the rollback path actually safe once
some fraction of users are on the new path — can old and new code paths coexist against the same
data, or does rolling back require a data fix too?
```

## What NOT to do

- Don't write a plan whose stages are only about traffic percentage if the change also involves a
  data migration — data-path rollback safety is a separate, often harder question that a
  percentage-only plan silently ignores.
- Don't set rollback triggers so loose they'd never actually fire, or so tight normal noise trips
  them — both defeat the purpose; ground thresholds in this system's actual normal variance if
  that's known, and say so honestly if it isn't known yet.
