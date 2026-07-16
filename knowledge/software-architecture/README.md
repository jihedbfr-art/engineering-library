# 🏗️ Software Architecture

Not "which framework" — the decisions that outlive any single framework: how you carve up a system's boundaries, where business logic actually lives, and how you keep a codebase changeable after the first six months, when the "just ship it" phase is long over and every change starts fighting the last six months of shortcuts.

- [domain-driven-design.md](domain-driven-design.md) — bounded contexts, ubiquitous language, aggregates — modeling complexity instead of drowning in it
- [hexagonal-clean-architecture.md](hexagonal-clean-architecture.md) — keeping business logic independent of frameworks, databases, and delivery mechanisms
- [event-sourcing-cqrs.md](event-sourcing-cqrs.md) — storing what happened instead of only current state, and why that's sometimes exactly what you need

## Why this is its own section, separate from computer-science/design-patterns

[Design patterns](../computer-science/design-patterns.md) are reusable solutions to recurring *local* problems — one class, one small interaction. Architecture is about the *system-wide* shape: where do boundaries go, what depends on what, what can change independently of what else. Get the patterns right inside a badly-shaped architecture and you've just written very elegant code that's still tangled at the system level — architecture is the layer patterns can't fix by themselves.
