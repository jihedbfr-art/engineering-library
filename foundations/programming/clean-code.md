# Clean Code — Principles for Every Language

Code is read far more than it's written. Optimize for the next human (often future-you).

## Naming — the highest-leverage habit

```
❌ d, tmp, data2, mgr, doStuff()
✅ elapsedDays, activeUsers, retryCount, sendWelcomeEmail()
```
- Names should reveal **intent**. If you need a comment to explain a name, rename it.
- Booleans read as yes/no: `isActive`, `hasPermission`, `canRetry`.
- Functions are verbs, variables are nouns. Be consistent (`fetch`/`get`/`load` — pick one meaning).

## Functions

- **Small.** One thing, one level of abstraction. If you scroll, split it.
- **Few parameters.** 0–2 ideal; 3+ suggests a missing object.
- **No surprising side effects** — a function named `getUser` shouldn't also write to the DB.
- **Return early** to avoid nested pyramids:
```python
def process(order):
    if not order:        return
    if order.paid:       return
    # main logic un-indented
```

## Comments

- Good code needs few comments — the code says *what*, comments say *why*.
```python
# ❌ increment i
i += 1
# ✅ skip the header row the vendor prepends to every export
i += 1
```
- Delete commented-out code. Git remembers it. Dead code is noise and lies.

## DRY — but don't over-abstract

- Don't Repeat Yourself: duplicated logic → one source of truth.
- **But**: two things that look similar today may diverge tomorrow. A little duplication beats the wrong abstraction. Wait for the third occurrence before extracting.

## The SOLID principles (OO design)

| | Principle | In one line |
|---|---|---|
| **S** | Single Responsibility | one reason to change per class |
| **O** | Open/Closed | extend without modifying existing code |
| **L** | Liskov Substitution | subtypes must honor the base type's contract |
| **I** | Interface Segregation | small focused interfaces > one fat one |
| **D** | Dependency Inversion | depend on abstractions, not concretions |

## Error handling

- Fail fast and loud in development; degrade gracefully in production.
- Don't swallow exceptions silently (`catch {}` is a crime scene).
- Error messages should say what happened **and** what to do about it.

## The boy scout rule

> Leave the code a little cleaner than you found it.

Small, constant improvements beat mythical "big refactor" projects that never happen. Rename one bad variable, split one giant function, delete one dead file per PR.

## What "clean" is really for

Not aesthetics — **change**. Clean code is code you can modify safely and quickly. Every principle here serves that: the next person changing this code (probably you, in six months) does it without fear.
