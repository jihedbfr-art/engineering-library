# Billing Fundamentals

## The end-to-end chain

```
┌──────────┐   ┌───────────┐   ┌────────┐   ┌──────────┐   ┌─────────┐   ┌──────────┐
│ Network  │──►│ Mediation │──►│ Rating │──►│ Charging │──►│ Billing │──►│ Payment  │
│ (usage)  │   │ collect + │   │ price  │   │ apply to │   │ invoice │   │ collect  │
│          │   │ normalize │   │ events │   │ account  │   │ + taxes │   │ money    │
└──────────┘   └───────────┘   └────────┘   └──────────┘   └─────────┘   └──────────┘
                                                     │
                                              Revenue Assurance
                                            (did we bill it all?)
```

## Prepaid vs Postpaid — the fundamental split

| | **Prepaid** | **Postpaid** |
|---|---|---|
| Pay | Before use (top up a balance) | After use (monthly invoice) |
| Control | **Real-time** — check balance, authorize, decrement *during* the session | Collect records, bill at month end |
| System | **OCS** (Online Charging System) | Billing system (offline/batch) + optional real-time |
| Risk | Operator risk ≈ zero (no balance, no service) | Credit risk (customer may not pay) |
| Latency need | Milliseconds (block/allow live) | Relaxed (nightly batch OK) |

Prepaid dominates emerging markets; postpaid dominates mature/enterprise markets. Modern systems ("convergent charging") handle both on one platform.

## Convergent charging

One rating/charging engine for **all** services (voice, SMS, data, content) and **both** payment models (pre/post), across all access types (mobile, fixed, broadband). Benefits: a single balance, unified offers, one customer view. This is the modern target architecture.

## What gets billed — the event types

- **Voice**: duration-based (per second/minute), with setup fees, peak/off-peak.
- **SMS/MMS**: per message.
- **Data**: per volume (MB/GB), or flat with throttling after a cap.
- **Content/VAS**: subscriptions, one-off purchases, third-party (settled with partners).
- **Roaming**: usage on another operator's network — priced via inter-operator agreements.

## Tariff building blocks

- **Rate**: price per unit (e.g. €0.02/MB).
- **Bundle/Allowance**: included units (e.g. 50 GB) consumed before rating kicks in.
- **Discount/Promotion**: percentage off, free periods, loyalty rewards.
- **Cross-product rules**: "unlimited nights", "free on-net calls", family shared data.

The **product catalog** (in [BSS](../oss-bss.md)) defines all of this; the rating engine executes it.

## Key terms

| Term | Meaning |
|---|---|
| **CDR** | Call Detail Record — the raw usage event (who, what, when, how much) |
| **Rating** | Applying tariff logic to compute a monetary amount |
| **Charging** | Applying that amount to a subscriber's account/balance |
| **Billing** | Aggregating charges into an invoice (with taxes, discounts) |
| **Balance** | Prepaid credit available (money or units) |
| **Bill cycle** | The recurring period an invoice covers |
