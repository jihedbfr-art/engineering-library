# Git Workflows

*How* a team uses git matters as much as knowing the commands ([git cheatsheet](../resources/cheatsheets/git.md)). Here are the strategies teams actually run.

## Trunk-based development (the modern default)

Everyone integrates into `main` frequently (at least daily) via short-lived branches.

```
main ──●──●──●──●──●──●──▶   (always releasable)
        \   /  \   /
      feature  feature       (hours to ~2 days, then merged)
```

✅ Fewer painful merges, continuous integration, fast feedback, pairs perfectly with CI/CD and feature flags.
⚠️ Requires solid automated tests and small changes. Needs discipline.

**Feature flags** make it work: merge incomplete work behind a flag that's off in production, finish it over several PRs, flip it on when ready. Decouples deploy from release.

## GitHub Flow (simple, great for web/continuous deploy)

1. Branch off `main` (`feature/tags`).
2. Commit, push, open a Pull Request.
3. CI runs; teammates review.
4. Merge to `main` → auto-deploy.

Minimal ceremony. The default for most web teams and open source.

## Git Flow (heavier, for versioned releases)

Long-lived `main` + `develop`, plus `feature/*`, `release/*`, `hotfix/*` branches. Designed for software with scheduled versioned releases (installers, libraries).

⚠️ Often **overkill** for web apps that deploy continuously — the extra branches add merge overhead. Don't cargo-cult it; use it only if you ship discrete versions.

## Comparison

| | Trunk-based | GitHub Flow | Git Flow |
|---|---|---|---|
| Branch life | hours | days | weeks |
| Best for | CI/CD, high velocity | web apps, OSS | versioned releases |
| Complexity | low (needs flags+tests) | low | high |
| Merge pain | minimal | low | higher |

## Commit hygiene (any workflow)

- **Small, atomic commits** — one logical change each. Easy to review, revert, bisect.
- **Conventional messages**: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`. Enables automated changelogs/versioning.
- **Clean history before pushing shared branches** — squash the "wip", "oops", "fix typo" noise via interactive rebase. Never rebase already-shared history.

## Protecting main (do this)

- Require PR + at least one approval.
- Require green CI (tests, lint, security scans) before merge.
- Require branches up to date with main.
- No direct pushes to main — even for solo projects, the diff review catches your own mistakes.

## Choosing

Most teams should start with **GitHub Flow or trunk-based + feature flags**. Reach for Git Flow only if you genuinely ship versioned releases. The goal is always the same: integrate small changes often, keep `main` releasable, automate the checks.
