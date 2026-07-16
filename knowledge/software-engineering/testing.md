# Testing

Tests are how you change code without fear. They're not about "proving it works" — they're about catching regressions when you (inevitably) touch it later.

## The testing pyramid

```
        /\        E2E tests — few, slow, full system (Playwright, Cypress)
       /  \
      /____\      Integration tests — some, medium (API + real DB, service + service)
     /      \
    /________\    Unit tests — many, fast, isolated (a function/class, deps mocked)
```
Many fast unit tests, fewer integration, a handful of E2E. Invert the pyramid (mostly slow E2E) and your suite becomes flaky and glacial.

## What each level catches

| Level | Tests | Speed | Catches |
|---|---|---|---|
| **Unit** | one function/class, mocked deps | ms | logic bugs, edge cases |
| **Integration** | components together, real DB | seconds | wiring, queries, contracts |
| **E2E** | full app through the UI/API | minutes | "does the whole flow work" |

## Anatomy of a good test — Arrange, Act, Assert

```python
def test_moving_note_to_trash_sets_deleted_flag():
    note = Note(title="Draft", deleted=False)      # Arrange
    service.move_to_trash(note)                     # Act
    assert note.deleted is True                     # Assert
```
One behavior per test. The name states the behavior. If it needs many asserts of unrelated things, split it.

## What to test (and what not to)

✅ Business logic, edge cases (empty, null, boundary, negative), error paths, bug regressions (every fixed bug gets a test), public API/contracts.
❌ Framework internals, trivial getters, third-party libraries, implementation details that change often (test behavior, not internals).

## Test doubles

- **Mock** — a fake you assert *interactions* on ("was `send()` called once?").
- **Stub** — returns canned data so the test can proceed.
- **Fake** — a working lightweight impl (in-memory DB).
Mock at boundaries (network, DB, time, randomness) — not everything. Over-mocking tests the mocks, not the code.

## TDD — red, green, refactor

1. **Red** — write a failing test for the behavior you want.
2. **Green** — write the minimum code to pass it.
3. **Refactor** — clean up, tests stay green.

Benefits: forces you to define "done", produces testable design, gives instant regression safety. You don't have to TDD everything — but for tricky logic, writing the test first clarifies the problem.

## Coverage — a tool, not a target

Coverage shows what code ran during tests, not whether it's *well* tested. 100% coverage with weak asserts is theater; 70% on the critical paths beats 95% on getters. Chase meaningful assertions, not the number.

## Traits of a healthy suite

- **Fast** — runs in seconds, so people actually run it.
- **Deterministic** — no flaky tests (no real time/network/random without control). A flaky test gets ignored, then everything gets ignored.
- **Independent** — any order, no shared state.
- **Readable** — a failing test tells you what broke without debugging.
- **In CI** — runs on every push, blocks merge on red → [CI/CD](../devsecops/ci-cd/github-actions.md).
