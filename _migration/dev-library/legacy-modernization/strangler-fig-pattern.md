# The Strangler Fig Pattern

Named after the strangler fig vine, which grows around a host tree and gradually replaces it without the tree ever falling down. Applied to software: **build the new system around the edges of the old one, route traffic to it feature by feature, and retire the old system only once nothing depends on it anymore.** The alternative — stop everything, rewrite it all, cut over on a fixed date — is the single most common cause of failed modernization projects, and it fails for a predictable reason: a full rewrite has to correctly reproduce years of accumulated business logic before it can go live at all, with zero incremental validation along the way.

## The core mechanism

```
                        ┌──────────────┐
   All requests ───────►│    Facade /   │
                        │    Router     │
                        └───────┬──────┘
                    ┌───────────┴────────────┐
                    ▼                        ▼
            ┌───────────────┐       ┌────────────────┐
            │ Legacy system  │       │  New system     │
            │ (shrinking)    │       │  (growing)      │
            └───────────────┘       └────────────────┘
```
A routing layer sits in front of both systems. At the start, it sends 100% of traffic to the legacy system. As each capability is rebuilt and validated in the new system, the router starts sending *that specific capability's* traffic to the new system instead — while everything not yet migrated keeps flowing to the old one, completely undisturbed.

## Applying it to a real legacy system (the J2EE/Oracle ADF case)

Take a government tax/utility ERP built on Java 6, Oracle ADF, PL/SQL business logic — the kind of system where the business rules live partly in Java, partly in database packages, accumulated over a decade of ad-hoc client-requested changes.

```
Module: Administration (profiles/access) ──► migrate FIRST
        (self-contained, low business-rule complexity, low risk)

Module: Reporting/editions (client-requested reports) ──► migrate SECOND
        (mostly read-only, easy to validate output against the old system)

Module: Core declaration/payment logic (E-impôts style) ──► migrate LAST
        (dense business rules, direct financial/regulatory impact,
         highest cost of getting it wrong)
```
The ordering isn't arbitrary: **migrate low-risk, self-contained modules first** — partly to deliver real value early, but mostly to build the team's confidence and tooling (routing, monitoring, rollback) on lower-stakes ground before touching the modules where a mistake has real financial or regulatory consequences.

## The router/facade — the piece that makes this actually work

For a web ERP, this is often as simple as a reverse-proxy routing rule per URL path or feature flag:
```
/admin/**        → new system (once migrated)
/declarations/**  → legacy system (until migrated)
/reports/**       → new system
```
For deeper integration (shared session, shared database), the facade sometimes needs to translate between old and new data models mid-flight — this translation layer is usually the trickiest engineering in the whole pattern, and it's temporary by design: it exists only until the old system is fully retired.

## Data — the part that's harder than the code

The two systems typically need to **share or synchronize data** during the transition — the old PL/SQL-heavy schema and a new system's schema rarely match cleanly.

```
Approach 1 — Shared database, gradually migrated schema
  Both systems read/write the same tables during transition.
  Simple to start; the schema itself becomes the constraint that
  slows down how independently the new system can evolve.

Approach 2 — Data sync (CDC / scheduled jobs)
  Old system stays authoritative for its own modules;
  changes replicate to the new system's schema, and vice versa
  for already-migrated modules. More engineering upfront,
  but the new system's schema isn't held hostage by the old one.
```
There is no clean answer here — pick based on how tightly coupled the modules actually are in the current system, which is exactly why the requirement archaeology in [legacy-migration-playbook](legacy-migration-playbook.md) has to happen before this decision, not after.

## Why this beats a big-bang rewrite, concretely

1. **Continuous validation** — each migrated module goes live against real production traffic and gets caught if wrong, instead of every module's correctness being an open question until one single cutover day.
2. **Rollback is scoped** — if the newly-migrated reporting module has a bug, you route reporting traffic back to legacy while you fix it; a big-bang rewrite's rollback plan is usually "revert everything," which is often not realistically possible once real data has flowed through the new system for even a day.
3. **The team ships value throughout**, not just at the very end — stakeholders see progress module by module instead of betting an entire budget on one distant go-live date.

## When strangler fig is NOT the right call

- The legacy system is small enough that a full rewrite genuinely fits in a short, well-understood timebox — the pattern's overhead (routing layer, dual-running, data sync) isn't worth it below a certain size.
- The old system is so tightly coupled internally that no clean seam exists to strangle around — sometimes true, though it's worth treating as a hypothesis to test rather than an assumption to accept immediately; tightly-coupled legacy systems often have more seams than they first appear to.

## Where this connects

This is the same underlying idea as [core network migration](../telecom/core-network-migration.md)'s dual-running-cores approach — different domain, identical structural pattern: **run both systems in parallel, migrate in controlled increments, validate each increment before the next.** If you understood one, you understood both.
