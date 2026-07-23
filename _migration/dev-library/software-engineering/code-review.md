# Code Review

Code review catches bugs, spreads knowledge, and keeps the codebase coherent. Done badly it's a bottleneck and a morale sink. Here's how to do it well — on both sides.

## As the author (make it easy to review)

1. **Small PRs.** 200–400 lines max. A 2000-line PR gets a rubber-stamp, not a review. Split by concern.
2. **Write a real description**: what changed, why, how to test, screenshots if UI. The reviewer shouldn't reverse-engineer intent.
3. **Review your own diff first** — catch the obvious before a human spends time.
4. **One concern per PR.** Don't mix a refactor with a feature with a formatting sweep — it hides the real change.
5. **Green CI before requesting review.** Don't make humans find what the machine would.

## As the reviewer (be useful, be kind)

- **Review promptly.** A PR blocked for two days blocks a person. Timeboxed, frequent reviews beat perfect-but-late.
- **Distinguish blocking from optional.** Prefix nitpicks: `nit:` (optional), vs "this is a bug" (blocking). Don't hold a PR hostage over a variable name.
- **Ask, don't command.** "What happens if `items` is empty here?" invites thinking; "this is wrong" invites defensiveness.
- **Praise good things.** Review isn't only fault-finding.
- **Approve when it's better than what's there**, not when it's perfect. Perfect PRs never merge.

## What to actually look for (priority order)

1. **Correctness** — does it do what it claims? Edge cases (empty, null, concurrent, large input)?
2. **Security** — injection, authz checks, secrets, untrusted input → [OWASP Top 10](../devsecops/security/owasp-top10.md).
3. **Tests** — is the new behavior covered? Do tests actually assert something?
4. **Readability** — will someone understand this in 6 months? → [clean code](../programming/clean-code.md)
5. **Design** — does it fit the architecture, or bolt on tech debt?
6. *Then* style — and style should be automated (formatter/linter), not debated in review.

## Anti-patterns to avoid

- ❌ The **drive-by "LGTM"** without reading — worse than no review (false confidence).
- ❌ **Bikeshedding** — endless debate on trivia while real issues pass.
- ❌ **Rewriting the author's code in comments** — suggest, don't dictate their style.
- ❌ **Ego reviews** — using review to show dominance. Review the code, not the coder.

## The healthy default

Automate everything a machine can check (format, lint, types, tests) so humans review what only humans can: **is this correct, secure, clear, and well-designed?** That's where review earns its cost.
