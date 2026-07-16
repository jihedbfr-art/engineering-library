# Event Sourcing & CQRS

Two related, often-paired patterns that solve genuinely real problems for the right systems — and get reached for in situations that don't actually need them far more often than they should, given how much complexity they add.

## Event Sourcing — storing what happened, not just the current state

```
Traditional approach:              Event-sourced approach:
  accounts table:                    events table:
  id | balance                       account_id | event_type | amount | timestamp
  1  | 150                           1 | AccountOpened     | 0    | 2026-01-01
                                      1 | MoneyDeposited     | 200  | 2026-01-03
  (the HOW we got to 150             1 | MoneyWithdrawn     | 50   | 2026-01-10
   is completely gone —              (current balance = 150, DERIVED by replaying
   overwritten by every                every event, never stored directly at all)
   update along the way)
```
Instead of storing only current state (and overwriting it on every update, permanently losing history in the process), you store the **complete, immutable sequence of events** that led to that state — current state becomes a *derived*, replayable value, computed by folding over the event stream, never the actual source of truth itself.

## Why you'd deliberately choose this — the genuine, concrete benefits

- **Full audit trail, for free, by construction** — not a bolted-on separate audit log that can drift out of sync with the real data; the event log *is* the actual history, guaranteed complete, because it's the only thing ever written.
- **Time travel** — reconstruct exactly what the state looked like at any past point in time, precisely, by replaying events only up to that timestamp. Genuinely valuable for financial systems, compliance-heavy domains, and — very concretely — [telecom revenue assurance reconciliation](../../telecom/billing/revenue-assurance.md), where "what did the account actually look like at time T" is a real, recurring, sometimes disputed question.
- **New read models without touching write logic at all** — need a completely new way to view/aggregate the exact same underlying data? Build a new projection that replays the *same* existing events differently. The write side (what actually happened) never changes; only how you choose to interpret it does.
- **Genuine debugging superpower** — "why is this account's balance wrong" becomes "replay the events and watch, step by step, exactly where it diverges from expected" — a fundamentally more tractable debugging question than staring at one final, opaque number with zero visible history behind it.

## The real cost — and it's a genuine one, not a minor footnote

```
Query "what's the current balance" now requires:
  1. Load ALL events for this account (or a periodic snapshot + events since then)
  2. Replay them, in order, to derive current state
  3. Return the result

This is meaningfully more complex than "SELECT balance FROM accounts WHERE id = ?"
```
For accounts with genuinely long histories, replaying every single event on every single read becomes a real, measurable performance problem — the standard mitigation is periodic **snapshots** (materialize and cache current state every N events, then only replay events *since* the last snapshot) — but that's real, additional engineering complexity layered on top of an already more complex system, not a free architectural upgrade.

## CQRS — Command Query Responsibility Segregation

```
Traditional:                        CQRS:
  Same model handles BOTH             Command side: optimized for WRITES
  reads and writes                      (validates business rules, appends events)
                                       Query side: optimized for READS
                                        (denormalized, pre-joined, fast — often
                                         a completely different data store entirely)
```
The core insight: **read and write workloads frequently have fundamentally different optimal shapes**, and forcing one single model to serve both well is a real, recurring compromise. Writes typically want to enforce strict business rules and invariants on a normalized, carefully-modeled structure (see [aggregates](domain-driven-design.md)); reads typically want denormalized, pre-joined, fast-to-query data shaped exactly for a specific UI or report — CQRS simply stops pretending these are the same problem and lets each side be architected for what it actually needs.

## Event Sourcing + CQRS together — the pattern people usually actually mean

```
Command ──► Command handler ──► validates, appends EVENT to the event store
                                       │
                                       ▼ (async, via the event stream)
                              Projection builder ──► updates a denormalized
                                                       READ model (a separate
                                                       database/table, shaped
                                                       exactly for fast queries)
                                       │
Query ──────────────────────────────────► reads directly from the fast, denormalized
                                            read model — NEVER replays raw events
```
This is genuinely elegant once it fits: writes append immutable events (the actual source of truth); a background process asynchronously builds and maintains one or more read-optimized projections from that same event stream; queries hit the fast projection directly, never touching raw event replay on the hot query path at all. The tradeoff this introduces, explicitly: the read model is now **eventually consistent** with the write side — a very recent write might not be reflected in the read model for a brief window, which is exactly the kind of tradeoff [CAP-theorem thinking](../computer-science/system-design.md) forces you to make deliberately, not accidentally.

## When these patterns are genuinely the right call

✅ **Financial/accounting systems** — where a complete, immutable audit trail isn't a nice-to-have, it's often an actual regulatory requirement.
✅ **Domains with real regulatory/compliance audit requirements** — [telecom billing/revenue assurance](../../telecom/billing/revenue-assurance.md) is a textbook, concrete fit.
✅ **Collaborative systems needing genuine conflict resolution** — event logs make "what actually happened, and in what order" an explicit, first-class fact instead of an implicit, easily-lost one.
✅ **Read and write loads that are genuinely, dramatically different in shape and scale** — CQRS alone (without full event sourcing) can be worth adopting even without going all the way to a full event-sourced write model.

## When they're very much not — and this is the more common real-world case

⚠️ A standard CRUD application with straightforward reporting needs. ⚠️ A team without the operational maturity to run eventual consistency, snapshotting, and event schema evolution correctly and confidently. ⚠️ "We might need an audit log someday" — a real, hypothetical future need is not, by itself, sufficient justification for this much architectural complexity paid for today. **These patterns are reached for far more often than the underlying problem actually justifies** — this is one of the clearest, most common instances of architecture astronautics in the entire industry: genuinely powerful tools, applied to problems that a normal relational table with a straightforward audit_log column would have solved more simply, more cheaply, and with a much smaller team of engineers actually able to maintain it confidently.

## Where this connects

Both patterns build directly on [DDD's aggregates](domain-driven-design.md) — an aggregate is the natural, coherent unit an event stream is scoped to. Both also lean on the same [eventual consistency and event-driven thinking](../../data-engineering/streaming-kafka.md) covered in the data engineering section — Kafka is, in fact, a very natural, common technology choice for the underlying event store itself, given it's already built around exactly this "immutable, ordered, replayable log" model.
