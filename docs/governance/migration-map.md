# Migration map — dev-library → engineering-library

Consolidation date: 2026-07-23. `dev-library` history was imported via `git subtree`
under `_migration/dev-library` (commit `c258f92`) to keep authorship and history intact,
then reorganized in place. This file tracks where every top-level folder ended up and why.

## Dropped (duplicate of existing engineering-library content)

Each of these had a same-topic, comparable-depth equivalent already living in
`knowledge/`, so keeping both would have meant two competing "canonical" pages on the
same subject. The existing `knowledge/` version stays; the incoming one was removed
rather than merged line by line, since neither text had a paragraph the other was
missing — they were two independent write-ups of the same ground.

| Dropped from dev-library | Kept instead | Why |
|---|---|---|
| `backend/` (apis, java-spring, microservices, nodejs) | `knowledge/backend/` | Same file names, same topics; `knowledge/backend` already covers more (grpc-and-graphql, resilience4j, spring-boot-vs-quarkus-vs-micronaut). |
| `devsecops/` (ci-cd, containers, iac, monitoring, security) | `knowledge/devsecops/` | Identical subfolder layout; `knowledge/devsecops` already has ansible-basics, distributed-tracing, supply-chain-security on top. |
| `software-architecture/` (ddd, event-sourcing-cqrs, hexagonal-clean-architecture) | `knowledge/architecture-library/` | Same three patterns, `architecture-library` already splits them further (12 pattern pages vs 3) and carries templates. |
| `telecom/` | `knowledge/telecom/` | Near-total filename overlap; `knowledge/telecom` already has everything dev-library had plus `billing/` and `edge-computing-mec.md`. |
| `projects/README.md` | `projects/` (existing tree) | Single-row index pointing at an external repo, no content of its own. |

## Moved as-is (complementary, no overlap)

| From dev-library | To | Why |
|---|---|---|
| `databases/*` (7 files: data-modeling, graph-databases, indexing-deep-dive, nosql, sql-essentials, transactions-concurrency, wide-column-cassandra) | `knowledge/database-engineering/` | Zero filename collision with the existing 7 files there (elasticsearch, mongodb, neo4j, oracle, postgresql, postgresql-vs-oracle, redis) — genuinely complementary coverage. |
| `ai/*` (82 files across 10 subfolders: foundations, RAG, agentic workflows, local inference, eval, hooks/skills, extensibility, guards, vector layer, model routing) | `knowledge/ai-engineering/` | Net-new domain, nothing to dedupe against. |
| `cybersecurity/*` (fundamentals, blue-team, pentest-methodology, web-security, learning-path) | `knowledge/cybersecurity/` | Different angle from `security-patterns` (how to build secure code) and `devsecops/security` (pipeline tooling) — this is offense/defense operational content, kept as its own sibling. |
| `software-engineering/{code-review,agile,testing,git-workflows}.md` | `knowledge/code-review/` and `knowledge/practices/` | No prior coverage of these topics in engineering-library. |
| `computer-science/, programming/, web/, networking/, cloud/, frontend/, mobile/, game-dev/, embedded-iot/, blockchain/, compilers/, data-engineering/, sre/, legacy-modernization/, resources/` | `foundations/` (new top-level tree) | Generalist "what is it / how does it work" material — answers a different question than `knowledge/`, which is "how do you implement, operate, and debug it here." Mirrors the split recommended in the July 2026 consolidation audit. |

## Rule going forward

- `foundations/` answers *"what is this and how does it work?"*
- `knowledge/` answers *"how do you choose, implement, operate, and debug this in production?"*
- Before adding a new page under either tree, check the other one first — if the topic
  already has a canonical home, extend that page instead of starting a second one.
