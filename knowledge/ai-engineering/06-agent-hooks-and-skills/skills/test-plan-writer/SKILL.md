---
name: test-plan-writer
description: Design a test plan for a feature or change — what to test, at which level (unit/integration/end-to-end), and which edge cases matter most. Use when asked to "write a test plan", "what should we test for this feature", or before implementing tests for a non-trivial change.
---

# Test plan writer

The point of a test plan is deciding *what's worth testing and at which level* before writing
any test code — skipping this step is how a feature ends up with either untested critical paths
or a pile of low-value tests that mostly re-verify the framework works.

## Process

1. **Identify the critical paths first.** What must work for this feature to deliver its actual
   value? Start there, not with the easiest thing to test — an easy-to-test getter/setter and a
   critical business rule don't deserve equal test investment.

2. **Pick the right level per behavior**, not by default:
   - **Unit test**: pure logic with no external dependency — business rules, calculations,
     validation logic. Fast, should be the majority of tests for anything with real logic.
   - **Integration test**: behavior that only makes sense with a real collaborator — a repository
     query against a real (or realistic in-memory) database, a message actually round-tripping
     through a queue.
   - **End-to-end test**: the handful of flows that matter enough to verify the whole system
     wired together correctly — reserve for critical user journeys, not every code path; e2e
     tests are slow and brittle by nature, over-relying on them is a common anti-pattern.

3. **Enumerate edge cases explicitly**, not just the happy path:
   - Boundary values (empty input, maximum size, zero, negative where unexpected)
   - Concurrent/race conditions if the feature touches shared state
   - Failure modes of every external dependency the feature calls (timeout, error response,
     malformed response) — a feature untested against its dependency failing is untested against
     the condition most likely to actually occur in production
   - Permission/authorization boundaries if the feature is access-controlled

4. **State what's explicitly out of scope**, and why — a test plan that doesn't say what it's
   NOT covering leaves that decision implicit and easy to accidentally violate later.

## Format

```
## Test plan: <feature/change>

### Critical paths (test first, most coverage)
- <path> — <level: unit/integration/e2e> — <why this level>

### Edge cases
- <case> — <expected behavior>

### Explicitly out of scope
- <what's not covered> — <why>
```

## What NOT to do

- Don't default everything to end-to-end tests because they're "more realistic" — that reasoning
  produces a slow, flaky suite; push logic-heavy verification down to unit level whenever the
  logic doesn't genuinely require the full system running.
- Don't list edge cases without stating the expected behavior for each — "test empty input" is
  incomplete without saying what should actually happen on empty input.
- Don't treat 100% coverage as the goal — the goal is confidence in the paths that matter;
  chasing coverage on trivial code (getters, framework boilerplate) is often wasted effort.
