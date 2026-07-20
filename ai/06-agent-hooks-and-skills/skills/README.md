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

## Scaffolding & generation

| Skill | What it produces |
|---|---|
| [`keycloak-spi-scaffold/`](keycloak-spi-scaffold/) | A working Provider/ProviderFactory/services-file scaffold for a Keycloak SPI module |
| [`documentation-generator/`](documentation-generator/) | README/module docs prioritizing what a new reader needs over restating the code |

## Writing & reporting

| Skill | What it structures |
|---|---|
| [`git-commit-message-writer/`](git-commit-message-writer/) | A commit message that explains *why*, from an actual diff — not a restatement of *what* |
| [`adr-writer/`](adr-writer/) | An Architecture Decision Record with honest alternatives and consequences, not a decision dressed up as obvious |
| [`incident-postmortem-writer/`](incident-postmortem-writer/) | A blameless postmortem — root cause as a system condition, never a person |
| [`test-plan-writer/`](test-plan-writer/) | What to test, at which level, before any test code gets written |
| [`rag-eval-report/`](rag-eval-report/) | Raw eval output turned into failure clusters and one ranked next action, not a wall of red rows |

## Why these specific eleven

A mix on purpose: a few grounded in one specific stack deeply (Spring Boot, Keycloak, telecom
provisioning — where generic advice would be too shallow to be useful), and several
domain-general ones (commit messages, ADRs, migrations, test plans, postmortems) that apply
regardless of what stack a project runs. That split mirrors the real shape of a useful skill
catalog: some skills need deep, narrow expertise; others need a rigorous *process* applied
consistently, and the value is in the process being followed the same way every time, not in
niche knowledge.
