# Rating & Charging

Two related but distinct steps:
- **Rating** = *how much does this usage cost?* (apply tariff logic → a monetary amount)
- **Charging** = *apply that amount to the customer's account* (decrement balance / add to bill)

## Rating — turning usage into money

Inputs: a clean usage record ([CDR](mediation.md)) + the applicable tariff from the [product catalog](../oss-bss.md).

```
CDR (60s call, on-net, 14:00) + tariff (on-net €0.05/min, off-peak 50%)
        → rated amount = 1 min × €0.05 × 0.5 = €0.025
```

Rating must handle:
- **Unit conversion** (seconds→minutes, bytes→MB) with correct rounding rules
- **Time-of-day / day-of-week** bands (peak/off-peak)
- **Zones** (on-net/off-net, national/international, roaming)
- **Allowances first**: consume included bundle units before charging money
- **Tiered pricing** (first 10 GB at X, then Y)
- **Discounts & promotions** stacked in the right order
- **Taxes** (often applied at billing, not rating — depends on jurisdiction)

## Charging — applying it to the account

| | **Offline charging** | **Online charging** |
|---|---|---|
| When | *After* usage (collect → rate → bill) | *During* usage (authorize in real time) |
| Model | Postpaid | Prepaid (and real-time postpaid controls) |
| System | Billing system | [OCS](ocs.md) |
| Interface (4G) | **Rf** (Diameter) | **Ro** (Diameter) |
| Failure mode | Bill is wrong next month | Service wrongly allowed/blocked *now* |

## The real-time challenge (prepaid)

For prepaid data, you can't rate *after* — the customer could burn €1000 of roaming before the record arrives. So online charging works in **quota reservations**:

```
1. Session starts → OCS reserves a quota (e.g. 10 MB worth of balance)
2. Device uses data against the reserved quota
3. Near exhaustion → request the next quota chunk (re-authorization)
4. Balance empty → deny further quota → session throttled/dropped
5. Session ends → return unused reservation to balance
```
This "grant, use, re-grant" loop runs continuously, in milliseconds, for millions of concurrent sessions. It's a distributed-systems problem: consistency of balance vs latency vs availability.

## Rating engine design notes (for backend folks)

- **Determinism**: same input + same tariff → same amount, always. Testable like any pure function ([see evals mindset](../../ai/05-evaluation-observability/evals-and-testing.md)).
- **Catalog-driven**: pricing lives in configurable data, not hard-coded — product teams change offers without code deploys.
- **Idempotency**: reprocessing a CDR must not double-charge (tie to [dedup](mediation.md)).
- **Auditability**: every rated amount must be explainable (which rule, which rate) for disputes and regulators.
- **Precision**: money math in decimal, never float — same rule as [databases](../../databases/sql-essentials.md).

## Standards

- **3GPP** defines the charging architecture (Diameter Ro/Rf, and 5G's converged charging over HTTP/2).
- **5G Converged Charging System (CCS)**: merges online+offline into one service-based function (CHF) — cloud-native, API-driven.
