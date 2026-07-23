# 🔧 Data Engineering

Moving, transforming, and storing data at scale, reliably. The plumbing that makes analytics, ML, and billing systems possible — unglamorous and absolutely critical.

- [pipelines-etl-elt.md](pipelines-etl-elt.md) — batch pipelines, ETL vs ELT, orchestration
- [streaming-kafka.md](streaming-kafka.md) — event streaming, Kafka concepts, when to use it
- [data-warehouses.md](data-warehouses.md) — warehouse vs lake vs lakehouse, modeling for analytics
- [data-quality.md](data-quality.md) — testing pipelines, contracts, catching bad data before it spreads

## Where this connects in the library

This section is the generic version of a very specific problem this library already covers: [telecom mediation](../telecom/billing/mediation.md) is a streaming ETL pipeline; [telecom revenue assurance](../telecom/billing/revenue-assurance.md) is data quality applied to money. If you understood those pages, you already understand half of data engineering — this section gives you the vocabulary and tools that show up everywhere else too (analytics, ML feature pipelines, event-driven microservices).

## The one-sentence version of the field

Get the right data, to the right place, in the right shape, on time, without silently corrupting it — at whatever scale the business actually needs (which is usually smaller than the hype suggests).
