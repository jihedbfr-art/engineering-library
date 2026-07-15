# OCS — Online Charging System

The real-time heart of prepaid (and modern convergent) charging. The OCS decides, **in the moment**, whether a subscriber can use a service and how much it costs — then updates the balance live.

## What it must do, in milliseconds

1. Receive a charging request ("subscriber X wants a data session").
2. Check the **balance/quota** and the subscriber's plan.
3. **Authorize** a usage quota (or deny).
4. Track consumption; **re-authorize** as quota runs low.
5. On session end, reconcile and **debit** the exact amount, return unused.

If the OCS is slow, calls drop and data stalls. If it's wrong, the operator gives away service or cuts off paying customers. It is a hard **low-latency, high-availability, strongly-consistent** system.

## Core components (3GPP model)

| Function | Role |
|---|---|
| **Online Charging Function (OCF)** | Handles the real-time charging dialog (Diameter **Ro**) |
| **Rating Function** | Prices the requested/used units ([rating](rating-charging.md)) |
| **Account Balance Mgmt (ABMF)** | The subscriber's balances and reservations |
| **Charging Gateway (CGF)** | Interfaces to downstream billing/records |

In 5G this converges into the **CHF (Charging Function)** of the Converged Charging System, exposed as a service-based (HTTP/2) network function.

## Quota reservation — the key mechanism

Instead of pricing every packet (impossible at scale), the OCS grants chunks:

```
Session start
   │ CCR-Initial  ─────────────►  reserve quota (e.g. €0.50 / 20 MB)
   │ CCA          ◄─────────────  granted
   ▼
Using data... approaching limit
   │ CCR-Update   ─────────────►  reserve next chunk
   │ CCA          ◄─────────────  granted (or denied → throttle)
   ▼
Session end
   │ CCR-Terminate ────────────►  debit actual usage, refund the rest
```
(CCR/CCA = Diameter Credit-Control Request/Answer.)

## Engineering properties (why it's a systems-design gem)

- **Consistency vs latency**: the balance is shared mutable state hit by many concurrent sessions. Naive locking kills throughput; you need careful reservation/partition strategies.
- **High availability**: an OCS outage means a national network can't authorize prepaid usage. Active-active clusters, geo-redundancy, graceful degradation ("allow and reconcile" fallbacks).
- **Idempotency & recovery**: network glitches cause retries — double-debits are unacceptable.
- **Massive concurrency**: millions of simultaneous sessions, each with its own quota loop.

## Real-time use cases the OCS enables

- **Balance-based cutoff** (prepaid): stop service exactly when credit runs out.
- **Spending caps** (postpaid): block/notify at a set limit — regulator-mandated in many regions to prevent bill shock.
- **Real-time notifications**: "80% of your data used" SMS.
- **Dynamic offers**: "you're out of data — buy 1 GB for €3?" at the moment of need.
- **Fair-use throttling**: drop speed after a cap instead of charging.

## Where it sits

Fed by [online mediation](mediation.md), driven by the [rating engine](rating-charging.md), governed by policy from the [PCF/PCRF](../network-architecture.md), reconciled by [revenue assurance](revenue-assurance.md). It's the busiest, most latency-critical node in the [billing chain](billing-fundamentals.md).
