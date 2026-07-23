# 🏛️ Legacy Modernization

Almost every "greenfield" tutorial in this library assumes you're starting from nothing. Most real engineering work isn't that — it's a 15-year-old system, still running the business, that has to keep running while you carefully change what it's built on. This section is drawn from real government ERP and enterprise systems (Oracle ADF/J2EE tax and utility platforms, telecom BSS migrations), not theory.

- [strangler-fig-pattern.md](strangler-fig-pattern.md) — replacing a legacy system incrementally, without a big-bang rewrite
- [legacy-migration-playbook.md](legacy-migration-playbook.md) — the actual project discipline: requirement archaeology, parallel running, cutover
- [working-with-legacy-code.md](working-with-legacy-code.md) — safely changing code you don't fully understand yet, with no tests
- [oracle-adf.md](oracle-adf.md) — a real stack most guides skip: architecture, where the real complexity hides, migrating off it

## The uncomfortable truth this section starts from

The "just rewrite it" instinct is almost always wrong, and it's wrong for a specific, recurring reason: a legacy system encodes years of accumulated business rules, edge cases, and quiet fixes for problems nobody remembers anymore. A rewrite that misses even a handful of those doesn't fail loudly in a demo — it fails quietly, months later, for one specific customer segment or one specific regulatory scenario, discovered by an angry support ticket instead of a test. Every page in this section starts from that premise: **the old system, however ugly, is usually the most accurate specification you have.** Read it before you replace it.
