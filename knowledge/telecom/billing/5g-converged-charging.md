# 5G Converged Charging (CHF / Nchf)

[ocs.md](ocs.md) and [rating-charging.md](rating-charging.md) cover the online/offline split as it's existed since 3G/4G — Diameter Ro for online, Rf for offline, two separate systems that usually meant two separate platforms, two separate teams, and a reconciliation headache between them. 5G's Converged Charging System collapses that split into one function. Worth its own page because the shift is bigger than "same charging, new protocol" — it changes how charging integrates with everything else in the core.

## What "converged" actually means

Pre-5G, online charging (prepaid, real-time balance check before service continues) and offline charging (postpaid, usage recorded and rated after the fact) ran on genuinely different rails — different protocol (Diameter Ro vs Rf), often different vendor platforms entirely. 5G's **CHF (Charging Function)** handles both through the same service-based interface: a session can be online-charged, offline-charged, or a mix (some services checked in real time, others just recorded), and it's a policy decision, not a platform decision. One function, one data model, one place operators look when a customer disputes a bill.

## The protocol shift: Diameter → HTTP/2 + JSON

This is the part that actually matters for anyone building against it. Diameter is a binary, telecom-specific protocol — AVPs (Attribute-Value Pairs), a purpose-built stack, purpose-built tooling to debug it. The 5G Service-Based Architecture replaces it with **Nchf**, an HTTP/2 + JSON API following standard REST conventions.

```http
POST /nchf-convergedcharging/v3/charging-data
Content-Type: application/json

{
  "subscriberIdentifier": "imsi-208930000000001",
  "nfConsumerIdentification": { "nFName": "SMF-01" },
  "invocationTimeStamp": "2026-07-19T10:15:00Z",
  "invocationSequenceNumber": 1,
  "multipleUnitUsage": [
    {
      "ratingGroup": 100,
      "requestedUnit": { "totalVolume": 104857600 }
    }
  ]
}
```
```json
// Response — CHF grants a quota, same "request a bucket, consume it, report back" logic
// as Diameter CCR/CCA-Initial, just over HTTP/2 instead of Diameter's AVP encoding
{
  "invocationTimeStamp": "2026-07-19T10:15:00Z",
  "invocationSequenceNumber": 1,
  "multipleUnitInformation": [
    {
      "ratingGroup": 100,
      "grantedUnit": { "totalVolume": 52428800 },
      "finalUnitIndication": { "finalUnitAction": "TERMINATE" }
    }
  ]
}
```

Practically: any backend developer who's built a REST resource server can read a CHF interface spec and mostly understand it on first pass — that was never true of a Diameter CCR/CCA exchange, which needed telecom-specific tooling (Wireshark with a Diameter dissector, dedicated test suites) just to inspect what was happening on the wire. This is the same "ordinary API integration" shift [CAMARA](../camara-network-apis.md) brings to network capabilities, applied to charging itself.

## Where CHF sits in the 5G core

```
UE session → SMF (Session Management Function) → Nchf → CHF
                                                          │
                                                    Rating engine, account balance
```

The SMF is what actually triggers a charging event (session start, quota exhausted, session end) — CHF doesn't watch traffic directly, it responds to what the SMF reports. This matters operationally: a charging gap almost always traces back to an SMF event that didn't fire or didn't reach CHF, not to a bug in the rating logic itself — check the event stream before assuming the pricing rules are wrong.

## Migration reality (not the vendor slide version)

Operators don't rip out a working 4G OCS/OFCS and replace it overnight — CHF typically rolls in as part of the 5G Standalone (SA) core buildout, running alongside the legacy Diameter-based charging for whatever traffic hasn't migrated yet (still-4G subscribers, roaming partners who haven't upgraded their signaling). That means, for a stretch that can run years, a real operator's charging estate is **both**: legacy Ro/Rf charging for the non-SA traffic, CHF/Nchf for 5G SA traffic, converging over time as SA coverage grows and partners catch up. Anyone building rating or revenue-assurance logic during that window has to reconcile usage arriving through two structurally different interfaces into one coherent view of what a customer actually owes — that reconciliation layer is where the real integration effort goes, not in standing up CHF itself.

## Related

- [ocs.md](ocs.md) — the online charging model CHF generalizes
- [rating-charging.md](rating-charging.md) — rating logic, independent of which protocol delivered the usage event
- [core-network-migration.md](../core-network-migration.md) — the broader shape of running two core generations side by side during a migration
