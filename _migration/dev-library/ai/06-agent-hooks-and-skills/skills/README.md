# Skills catalog

Real, complete `SKILL.md` packages — not the synthetic two-skill demo in
[`../skill_loader.py`](../skill_loader.py), actual reusable capability definitions following the
frontmatter-plus-instructions format described in
[`../skills-pattern.md`](../skills-pattern.md). Point any skill-aware assistant at one of these
directories, or read the `SKILL.md` directly as a structured checklist/prompt template even
without a loader — both are legitimate uses.

Each skill's `description` frontmatter is written to be matched on, not just read — see
[`../skills-pattern.md`](../skills-pattern.md)'s "What makes a good skill description" section
for why that field carries more weight than it looks like it should.

## Code & architecture review

| Skill | What it catches |
|---|---|
| [`spring-boot-code-review/`](spring-boot-code-review/) | Transaction self-invocation, silent rollback gaps, N+1 queries, unbounded external calls, resource leaks |
| [`api-contract-reviewer/`](api-contract-reviewer/) | Breaking API changes, wrong HTTP semantics, inconsistent error shapes, missing pagination/versioning |
| [`database-migration-reviewer/`](database-migration-reviewer/) | Rolling-deploy incompatible migrations, lock duration on real table sizes, non-reversible rollbacks |
| [`telecom-bss-integration-review/`](telecom-bss-integration-review/) | Idempotency gaps, partial-failure handling, and correlation-ID propagation in provisioning/portability flows |
| [`security-audit-checklist/`](security-audit-checklist/) | Injection, broken access control, unsafe deserialization, crypto misuse — defensive review only, never exploit code |
| [`dependency-upgrade-reviewer/`](dependency-upgrade-reviewer/) | Breaking-change risk in a version bump that green CI alone doesn't prove safe |

## Scaffolding & generation

| Skill | What it produces |
|---|---|
| [`keycloak-spi-scaffold/`](keycloak-spi-scaffold/) | A working Provider/ProviderFactory/services-file scaffold for a Keycloak SPI module |
| [`documentation-generator/`](documentation-generator/) | README/module docs prioritizing what a new reader needs over restating the code |

## Planning & diagnosis

| Skill | What it plans |
|---|---|
| [`code-refactoring-planner/`](code-refactoring-planner/) | A large refactor broken into small, independently-shippable, behavior-preserving steps |
| [`performance-profiling-guide/`](performance-profiling-guide/) | Measure-first diagnosis of a real bottleneck, before proposing any fix |
| [`test-plan-writer/`](test-plan-writer/) | What to test, at which level, before any test code gets written |

## Writing & reporting

| Skill | What it structures |
|---|---|
| [`git-commit-message-writer/`](git-commit-message-writer/) | A commit message that explains *why*, from an actual diff — not a restatement of *what* |
| [`adr-writer/`](adr-writer/) | An Architecture Decision Record with honest alternatives and consequences, not a decision dressed up as obvious |
| [`incident-postmortem-writer/`](incident-postmortem-writer/) | A blameless postmortem — root cause as a system condition, never a person |
| [`changelog-writer/`](changelog-writer/) | Commits/PRs grouped by user-facing impact, in plain language, breaking changes never buried |
| [`rag-eval-report/`](rag-eval-report/) | Raw eval output turned into failure clusters and one ranked next action, not a wall of red rows |

## Why this specific mix

A deliberate split: a few skills grounded in one specific stack deeply (Spring Boot, Keycloak,
telecom provisioning — where generic advice would be too shallow to be useful), and most of the
catalog domain-general (commit messages, ADRs, migrations, test plans, postmortems, refactoring,
performance, security, dependency review) that apply regardless of what stack a project runs.
That split mirrors the real shape of a useful skill catalog: some skills need deep, narrow
expertise; most need a rigorous *process* applied consistently, and the value is in the process
being followed the same way every time, not in niche knowledge.
