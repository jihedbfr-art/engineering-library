# 💰 Telecom Billing

How operators turn network usage into money. This is one of the largest, most demanding backend problem spaces in existence: billions of events per day, real-time balance checks, zero tolerance for revenue leakage.

## Files

- [billing-fundamentals.md](billing-fundamentals.md) — the end-to-end chain, prepaid vs postpaid
- [mediation.md](mediation.md) — collecting and normalizing usage records (CDRs)
- [rating-charging.md](rating-charging.md) — turning usage into cost; online vs offline
- [ocs.md](ocs.md) — Online Charging System: real-time balance and control
- [5g-converged-charging.md](5g-converged-charging.md) — CHF/Nchf: online+offline merged, Diameter replaced by HTTP/2+JSON
- [revenue-assurance.md](revenue-assurance.md) — plugging the leaks, fighting fraud

## The chain in one line

```
Network event → Mediation (collect/normalize) → Rating (price it)
→ Charging (apply to account) → Billing (invoice) → Payment → Revenue Assurance (verify)
```

## Why it's hard

- **Scale**: a national operator generates billions of usage records daily.
- **Real-time**: prepaid means checking and decrementing a balance *before* the call/data continues — in milliseconds, or you give away service or cut off a paying customer.
- **Correctness**: a 0.1% rating error across billions of events is a fortune lost or over-charged (and a regulator's attention).
- **Complexity**: tariffs, bundles, promotions, roaming, taxes, discounts, family plans — the pricing logic is genuinely intricate.
