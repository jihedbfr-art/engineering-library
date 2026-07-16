# Core Network Migration (Vendor Swap)

Migrating a live operator's core network from one vendor to another — say, Nokia to Huawei, or any incumbent to a new supplier — while millions of subscribers keep making calls throughout. This is one of the highest-stakes projects in telecom IT: zero tolerance for mass outage, and it can't be done in one shot.

## Why this happens

- Cost renegotiation / vendor diversification (operators rarely want single-vendor lock-in on something this critical).
- Technology generation change (e.g. moving to 5G-capable core requires new UDM/PCF that an old vendor's roadmap doesn't cover on the desired timeline).
- M&A / network consolidation after operator mergers.

## The core constraint that shapes everything

You cannot cut over a national subscriber base in one maintenance window. The migration has to run **both cores in parallel**, for weeks or months, with subscribers gradually moved batch by batch — and every system that talks to "the core" (provisioning, billing, IN/prepaid, number portability) has to work correctly regardless of which core a given subscriber currently lives on.

```
                    ┌─────────────────┐
   Migrated subs ──►│  New Core (X)   │
                    └─────────────────┘
   BSS / Provisioning / Billing
   (must route correctly per subscriber, throughout the whole migration)
                    ┌─────────────────┐
Not-yet-migrated ──►│  Old Core (Y)   │
                    └─────────────────┘
```

## The parallel-run architecture

1. **Dual-write / dual-connector period**: the provisioning layer (see [provisioning-architecture](provisioning-architecture.md)) gains connectors for *both* vendors' equipment, active simultaneously. New activations can be routed to either core based on migration policy.
2. **Subscriber routing logic**: every downstream system (billing, provisioning, care) needs to know *which core a given subscriber is currently on* — usually a lookup table or flag that gets checked before any operation. Get this wrong and you provision a change against the wrong core, and the subscriber's service silently doesn't update.
3. **Batch migration waves**: subscribers move in controlled batches (by region, by segment, by risk profile — never "everyone at once"), each wave validated before the next begins.
4. **Rollback capability per wave**: if a batch shows problems (call failures, provisioning errors, billing mismatches), that batch needs to be revertible back to the old core without touching subscribers who weren't in it.

## What actually gets migrated (the connector-by-connector reality)

Referring back to the [provisioning connector pattern](provisioning-architecture.md), a core migration is, very concretely, building and validating new connectors one network function at a time:

| Function | What changes |
|---|---|
| **HLR/HSS → new vendor's HLR/HSS (or UDM if also going 5G-ready)** | Subscriber identity, auth keys — the highest-risk migration, gets subscribers stuck unable to attach if done wrong |
| **EPS/VoLTE profile** | Voice service configuration — errors here mean subscribers with data but no calling |
| **PCRF → PCF** | Policy/quota enforcement — errors mean wrong data caps or QoS silently applied |
| **Provisioning logic itself** | Rebuilding the actual activation/modification workflows against the new vendor's API/protocol, not just pointing the old workflow at a new address — the old vendor's InstantLink/FlowOne-style flow logic doesn't map 1:1 onto a different vendor's provisioning model, so this is a genuine re-design, not a find-and-replace |

## The unglamorous 80%: requirement archaeology

Before writing a single new connector, the real work is **enumerating every existing request type currently flowing through the old provisioning logic** — every order type, every edge case, every undocumented business rule some connector quietly encodes because a subscriber segment needed it five years ago. Missing one of these doesn't fail loudly; it fails as a very specific subscriber segment silently getting the wrong service months later, discovered via a support ticket or a revenue-assurance report, not a test.

This is why a migration project spends so much time in **workshops with the current vendor's team and the operator's architecture group** — reverse-engineering "what does this system actually do today" is harder than building the replacement, because production behavior always diverges from documentation.

## Testing strategy — you cannot test this in a lab and call it done

- **Environment parity**: DEV/UAT environments for both old and new cores, kept genuinely representative of production configuration (a shockingly common failure mode is a test environment that's drifted from prod and hides a real bug).
- **Shadow testing**: route real (or replayed) provisioning traffic to the new core in parallel, compare results against the old core's actual response, without letting the new core's result affect the live subscriber — catches divergence before it's customer-facing.
- **Canary batches**: the first live migration waves are small and heavily monitored, specifically to catch the requirement gaps that only production traffic surfaces.
- **Reconciliation after every wave**: cross-check subscriber state on the network against BSS records for the migrated batch — this is [revenue assurance](billing/revenue-assurance.md) applied specifically to migration correctness, not just billing.

## Coordination — the part engineers underestimate

A migration this size runs on **weekly cross-team meetings** (kickoffs, feasibility reviews, workshops) coordinating: the new vendor's engineers, the old vendor's support (who often still need to fix bugs in the system being replaced, mid-migration), the operator's architecture/system teams, and IT support validating each wave. The technical work is maybe half the actual effort; the other half is making sure every team agrees on what "done" means for each connector before it goes live — a disagreement caught in a workshop is free, the same disagreement caught in production is an incident.

## The honest lesson

The failure mode in these projects is almost never "we didn't know the new vendor's API." It's "we didn't fully know what the old system actually did in production," discovered one edge case at a time, usually by a subscriber segment nobody thought to test. Budget the requirement-discovery phase like it's the hard part, because it is.
