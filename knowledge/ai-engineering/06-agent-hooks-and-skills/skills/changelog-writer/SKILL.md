---
format: "v2"
name: "changelog-writer"
title: "Changelog Writer"
title_fr: "Générateur de Changelog et Notes de Version"
description: "Write a changelog entry from a set of commits or merged PRs, grouped by user-facing impact rather than commit order, in plain language non-engineers can understand."
description_fr: "Rédige une entrée de changelog à partir de commits ou de PR fusionnées, regroupée par impact utilisateur plutôt que par ordre des commits, dans un langage clair pour les non-développeurs."
domain: "skills"
tags: [cybersecurity, engineering, best-practices]
maturity: "stable"
audience: ["backend-engineer", "security-engineer", "coding-agent"]
requires: ["bash", "git"]
updated: "2026-08-08"
---

## Prerequisites
- Repository codebase checked out locally.
- Access to Java 17+, Spring Boot 3+, or target framework environment.
- Required build tools (Maven/Gradle) installed.

## Usage
A changelog is for the person consuming the software, not for the team that built it — the
audience test that shapes every rule below. A list of commit messages is not a changelog; it's a
git log with different formatting.

#### Process

1. **Filter for user-facing impact.** Internal refactors, dependency bumps with no behavior
   change, and test additions generally don't belong in a user-facing changelog — unless the
   audience is specifically other developers integrating against this as a library/API, in which
   case internal changes that affect integration (a changed internal API surface) do matter.
   Confirm which audience before filtering.

2. **Group by category, not commit order:**
   - **Added** — new capability
   - **Changed** — existing behavior that's different now
   - **Fixed** — a bug that's resolved
   - **Deprecated** — still works, but shouldn't be relied on going forward
   - **Removed** — no longer available
   - **Security** — a vulnerability that's been addressed (state impact without exploit detail,
     same rule as [security-audit-checklist](../security-audit-checklist/SKILL.md))

3. **Write each entry from the user's perspective, in plain language.** "Fixed a race condition
   in the connection pool's release logic" (implementation detail) becomes "Fixed an issue where
   the app could become unresponsive under heavy concurrent use" (user-facing symptom) — describe
   what the user experienced or gained, not the internal mechanism, unless the audience is
   developers who specifically need the mechanism.

4. **Flag breaking changes unmistakably**, separate from routine changes — a breaking change
   buried in a "Changed" list among minor tweaks is a support-ticket generator waiting to happen.

#### Format

```markdown
#### [version] - YYYY-MM-DD

##### Added
- <user-facing capability, plain language>

##### Fixed
- <user-facing symptom that's resolved, plain language>

##### Changed
- <what's different, and what a user might need to adjust>

##### ⚠️ Breaking Changes
- <what breaks, what to do instead>
```

#### What NOT to do

- Don't include internal-only changes (test coverage, CI config, code style) in a user-facing
  changelog — they add noise without value to the intended reader.
- Don't copy commit messages verbatim into the changelog — a commit message explains *why* to a
  future engineer (see [git-commit-message-writer](../git-commit-message-writer/SKILL.md)); a
  changelog entry explains *what changed for you* to a user. Different audience, different
  wording, even for the same underlying change.
- Don't bury a breaking change in prose — it needs its own clearly marked section every time.

## Inputs
- Source code diff or repository path under evaluation.
- Relevant documentation, configuration files, or issue description.

## Outputs
- Structured review findings, action items, or generated markdown artifacts.
