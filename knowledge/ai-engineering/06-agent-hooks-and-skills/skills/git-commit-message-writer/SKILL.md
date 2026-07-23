---
name: git-commit-message-writer
description: Write a git commit message from a diff that explains why the change was made, not just what changed. Use whenever asked to "write a commit message", "commit these changes", or when preparing a commit and the diff is available.
---

# Commit message writer

A commit message's most valuable content is the part `git diff` can't show: *why*. The diff
already says what changed line by line — a message that just re-describes the diff in prose adds
nothing a reader couldn't get faster from the diff itself.

## Process

1. Read the actual diff, not just a description of it — the reasoning has to match what was
   really changed, not what was intended or assumed.
2. Identify the *motivation*: a bug being fixed (what broke, under what condition), a feature
   being added (what capability this unlocks), a refactor (what got easier/safer as a result), or
   a dependency/config change (why now, what triggered it).
3. Write a subject line under ~70 characters, imperative mood ("fix race condition in X" not
   "fixed" or "fixes"), specific enough that someone skimming `git log --oneline` understands the
   change without opening it.
4. If the why isn't obvious from the subject alone, add a body: 1-3 sentences, blank line
   separating it from the subject, explaining the reasoning — not a bullet list restating every
   changed file, that's what the diff is for.

## Format

```
<type>: <imperative summary, under ~70 chars>

<optional body: the why, in 1-3 sentences, only if the subject doesn't already make it clear>
```

Common `<type>` prefixes worth using consistently if the repo doesn't already have its own
convention: `fix`, `feat`, `refactor`, `docs`, `test`, `chore`. Match whatever convention the
repo's existing `git log` already uses instead of introducing a new one unprompted.

## What NOT to do

- Don't describe *what* changed file by file ("updated UserService.java, added a test, modified
  the README") — that's the diff, restating it wastes the one field that could carry reasoning
  instead.
- Don't write a commit message before actually reading the diff — a message inferred only from a
  task description risks describing intent rather than the actual resulting change, and the two
  can diverge (a fix that took a different shape than originally planned, a side effect the diff
  reveals that the task description didn't mention).
- Don't pad a trivial change with an unnecessary body — "fix typo in error message" needs no
  further explanation.
