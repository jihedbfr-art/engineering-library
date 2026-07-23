# Number Portability

The regulatory-mandated ability for a subscriber to switch operators **while keeping their phone number**. Simple from the user's side ("port my number"), genuinely intricate underneath — it's a real-time, multi-operator, regulator-supervised workflow with hard SLAs.

## Why it exists and why it's hard

Before number portability, changing operators meant changing your number — a real switching cost that protected incumbents from competition. Regulators mandated portability specifically to remove that friction. The difficulty: a phone number is no longer tied to "which operator owns this prefix" — it has to be **routable to whichever operator currently serves it**, and that mapping changes constantly.

## The central problem: routing after the number moves

```
Before port: +216-XX-XXXXXX → routes to Operator A (by number range ownership)
After port:   +216-XX-XXXXXX → must route to Operator B (ported) — but the
                                 number range still nominally "belongs" to A
```
Every call/SMS to a ported number needs a **portability lookup** before routing — a query to a shared/national database (or a local cache of it) that says "this specific number, despite its range, actually terminates at operator B now." Get this wrong and calls to millions of correctly-ported numbers route to the wrong network.

## The porting workflow — a real cross-operator state machine

```
1. Subscriber requests port at the RECEIVING operator (the one they're moving TO)
2. Receiving operator validates identity + account details
3. Port request sent to the DONOR operator (current operator) for validation
4. Donor operator confirms (or rejects — e.g. outstanding debt, wrong details)
5. Both operators + the central number portability registry coordinate
   the cutover at a scheduled time
6. Routing databases updated network-wide
7. Old service on donor network deactivated; new service on receiving
   network activated — timed to minimize the subscriber's downtime window
```
This is a **distributed transaction across two competing companies plus a neutral registry**, with a hard regulatory SLA (often measured in hours, sometimes mandated to be same-day). Neither operator fully controls the other's systems — the whole workflow is built on message exchange and agreed timing, not shared infrastructure.

## Systems involved, concretely

| System | Role |
|---|---|
| **NPG (Number Portability Gateway)** | The operator-side system that handles the porting protocol — sends/receives port requests, validates, triggers internal provisioning |
| **Central/national portability database** | The neutral, regulator-run source of truth for "which operator currently serves this number" — every network's routing consults this (directly or via synced local copies) |
| **Internal provisioning** ([see provisioning-architecture](provisioning-architecture.md)) | Once a port is confirmed, this is what actually activates/deactivates the subscriber's service on each operator's own core |
| **Billing/BSCS** | Must stop billing a donor-side subscriber and start on the receiving side, exactly in sync with the technical cutover — a timing mismatch here is a billing dispute waiting to happen |

## The failure modes that actually occur

- **Timing mismatch between technical cutover and billing cutover**: the subscriber gets billed by both operators for an overlapping window, or by neither — a direct [revenue assurance](billing/revenue-assurance.md) problem, and a fast way to generate complaints.
- **Stale routing caches**: if a network's local copy of the portability database isn't refreshed promptly, calls to a freshly-ported number briefly misroute — the reason near-real-time synchronization matters so much in this specific system.
- **Rejected ports stuck mid-workflow**: a donor operator can reject (e.g. contract not fulfilled, mismatched subscriber details) — the state machine has to handle "the whole thing rolls back cleanly" as a first-class outcome, not an edge case.
- **Migration of the portability system itself**: when an operator changes its NPG/portability platform (a genuinely disruptive, carefully staged project — old and new gateway logic have to agree on in-flight ports during the transition, exactly the dual-running discipline from [core-network-migration](core-network-migration.md)), every request type the old system silently handled has to be re-verified against the new one before cutover, or specific porting scenarios start failing without anyone noticing until complaints arrive.

## Fixed vs mobile portability — not quite the same problem

Mobile number portability (MNP) is the common case above. **Fixed-line portability** has its own wrinkles — numbers tied to a physical location/exchange rather than a subscriber identity, and porting sometimes constrained by geographic serviceability of the receiving operator's fixed infrastructure. The core "central registry + routing lookup" idea carries over; the validation rules differ.

## Where this connects

Number portability is a specialized case of [provisioning orchestration](provisioning-architecture.md) — multi-step, needs compensation on failure, high correctness stakes — except the "other system" you're coordinating with is a competitor's infrastructure instead of an internal network element. It's also a permanent source of [revenue assurance](billing/revenue-assurance.md) reconciliation work: billing and network state for ported numbers are exactly the kind of cross-system consistency check that catches real leakage.
