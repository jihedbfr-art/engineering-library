---
name: code-refactoring-planner
description: Plan a refactor as a sequence of small, independently-shippable, behavior-preserving steps rather than one large rewrite. Use when asked to "refactor this", "clean up this code", or "plan how to restructure this module" — especially for changes too large to land in one PR safely.
---

# Refactoring planner

The risk in a refactor isn't the end state — it's the path there. A big-bang rewrite that touches
everything at once is the hardest kind of change to review, test, and safely revert if something
goes wrong partway through. This skill's job is turning "refactor X" into an ordered sequence of
small steps, each one shippable and behavior-preserving on its own.

## Process

1. **Describe the end state first, briefly** — what the code should look like when done, so
   every subsequent step has a target, not just "make it better."

2. **Identify what can move independently.** Most refactors decompose into pieces with no real
   dependency on each other (extracting one class, renaming across one module, moving one
   responsibility) even if the *idea* feels monolithic. Find those seams before planning the
   sequence.

3. **Order steps so each one is safe to ship alone**, in this priority:
   - Steps that are purely mechanical and low-risk first (rename, extract method, move file) —
     build confidence and momentum, and are trivial to review.
   - Steps that change structure but not behavior next (introduce an interface, extract a class)
     — should be verifiable with existing tests, no new test coverage needed if done right.
   - Steps that change behavior (if any are truly necessary as part of "refactoring," they
     usually aren't — flag if a planned step isn't actually behavior-preserving, since that
     changes its risk profile and needs its own scrutiny, possibly its own plan).

4. **Call out what tests need to exist before a step is safe**, not after. A refactor on code
   with no test coverage is higher risk — if coverage is missing for the area being touched,
   that's a prerequisite step, not an afterthought.

## Output format

```
## Refactoring plan: <target>

### End state
<what the code looks like when this plan is complete>

### Steps (ordered, each independently shippable)
1. <step> — risk: low/medium — tests needed before: <existing/none/new tests X>
2. ...

### Explicitly NOT behavior-preserving
<any step that would actually change behavior, flagged separately with its own reasoning —
absent if the whole plan is behavior-preserving, which is the common and preferred case>
```

## What NOT to do

- Don't plan a refactor as one giant step "because splitting it doesn't make sense for this
  case" without first genuinely trying to find the seams — most refactors that feel atomic
  aren't, on closer look.
- Don't silently fold a behavior change into a "refactoring" step — if behavior changes, say so
  explicitly and treat it with the scrutiny a behavior change deserves (see
  [test-plan-writer](../test-plan-writer/SKILL.md)), don't let it hide inside a refactor's lower
  review bar.
- Don't propose steps that leave the codebase in a broken/non-compiling state between commits —
  each step should be a valid, working state on its own.
