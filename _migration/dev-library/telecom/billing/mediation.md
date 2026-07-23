# Mediation

The unglamorous, essential first step: **collect raw usage from every network element and turn it into clean, deduplicated, standardized records** the rating engine can price.

## Why it exists

The network speaks dozens of dialects. Each element (switch, gateway, IMS, SMSC) emits usage records in its **own vendor format**. Mediation is the translator and janitor between the messy network and the orderly billing systems.

```
   MSC ──┐
   SGSN ─┤                 ┌── normalize
   P-GW ─┼──► Mediation ───┼── deduplicate ──► clean CDRs ──► Rating / Billing
   IMS ──┤                 ├── filter / enrich
   SMSC ─┘                 └── correlate / aggregate
```

## What mediation actually does

1. **Collection** — pull/receive records from network elements (files, streams, real-time feeds).
2. **Decoding** — parse vendor/format-specific structures (often **ASN.1**-encoded binary).
3. **Normalization** — map everything to a common internal record format.
4. **Validation & filtering** — drop malformed/test/duplicate records.
5. **Deduplication** — the same call can be reported by multiple elements; count it once.
6. **Correlation/aggregation** — stitch partial records (a long session split into chunks) into one usage event.
7. **Enrichment** — add context (customer id, tariff hints) from reference data.
8. **Distribution** — route the clean record to rating, fraud, revenue assurance, analytics, etc.

## CDR — the atomic unit

A **Call Detail Record** (also xDR/EDR/IPDR for data) captures one usage event:

```
calling_number, called_number, start_time, duration,
bytes_up, bytes_down, cell_id, service_type, imsi, ...
```
Data-heavy networks generate staggering CDR volumes — this is a **big-data streaming** problem. Modern mediation increasingly uses stream processing (Kafka-style pipelines) rather than nightly batch files.

## Online vs offline mediation

- **Offline mediation**: batch collection of CDRs for postpaid billing and analytics. Latency-tolerant.
- **Online mediation**: real-time feed into the [OCS](ocs.md) for prepaid — must be instant.

## Where it connects

- **Downstream**: [rating & charging](rating-charging.md), [revenue assurance](revenue-assurance.md), fraud management, data warehouse.
- **A leak here = lost revenue everywhere**: if mediation drops or duplicates records, billing is wrong and RA has to catch it. Correctness is the whole job.

## For engineers

Mediation is essentially a **high-throughput ETL / stream-processing system** with hard correctness guarantees:
- exactly-once semantics matter (dedup)
- schema/format zoo → strong parsing and validation
- back-pressure and buffering (the network never stops emitting)
- observability on record counts at every stage (a sudden drop = incident)

If you've built data pipelines, you already understand 80% of mediation — see [../../devsecops/monitoring/observability.md](../../devsecops/monitoring/observability.md).
