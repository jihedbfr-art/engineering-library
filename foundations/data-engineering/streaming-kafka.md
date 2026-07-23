# Streaming & Kafka

Batch pipelines process data in scheduled chunks ("run every hour"). Streaming processes data **as it happens**, event by event, continuously. Kafka is the tool that made this mainstream.

## The mental shift: table vs stream

- **Batch/table thinking**: "what does the data look like *right now*?"
- **Stream thinking**: "what *happened*, in order, and when?"

A stream is a log of immutable events. A table is a snapshot you can derive *from* that log by replaying it. This reframing (championed by Kafka's creators) is why streaming architectures can rebuild any downstream view just by replaying history.

## Kafka core concepts

```
Producers ──► Topic (partitioned, ordered log) ──► Consumers
                 │
        Partition 0: [e1][e2][e3][e4]...
        Partition 1: [e1][e2][e3]...
        Partition 2: [e1][e2][e3][e4][e5]...
```

| Concept | What it means |
|---|---|
| **Topic** | A named stream of events (e.g. `order-events`) |
| **Partition** | A topic is split into partitions for parallelism; order is guaranteed *within* a partition, not across |
| **Producer** | Writes events to a topic |
| **Consumer / Consumer Group** | Reads events; a group splits partitions among its members for parallel processing |
| **Offset** | A consumer's position in a partition — how it tracks "what I've already read" |
| **Broker** | A Kafka server; a cluster is many brokers for durability and throughput |
| **Retention** | How long events stay on disk (time or size based) — Kafka isn't a queue that empties, it's a log you can replay |

## Producing and consuming (conceptually)

```python
# Producer
producer.send("order-events", key=order.customer_id, value=order.to_json())
# key matters: same key → same partition → preserved order for that customer

# Consumer
for event in consumer.poll("order-events", group="billing-service"):
    process(event)
    consumer.commit(event.offset)   # mark as processed
```
**Choosing a key matters**: partitioning by `customer_id` guarantees all of one customer's events are processed in order — critical when order matters (e.g. can't "cancel" an order before it's "created").

## Delivery guarantees — know which one you're getting

| Guarantee | Meaning | Risk |
|---|---|---|
| **At-most-once** | Might lose messages, never duplicates | Data loss |
| **At-least-once** | Never loses messages, might duplicate | Must handle duplicates downstream (idempotency again) |
| **Exactly-once** | Neither lost nor duplicated | Achievable within Kafka (transactions), harder across external systems |

Most real systems run **at-least-once** + **idempotent consumers** — same principle as [pipeline idempotency](pipelines-etl-elt.md) and [telecom mediation dedup](../telecom/billing/mediation.md). Trying to guarantee exactly-once everywhere is usually not worth the complexity.

## Stream processing — doing more than just passing messages through

- **Kafka Streams** / **ksqlDB**: transform, filter, join, and aggregate streams with a library/SQL, output to another topic.
- **Flink**: the heavier-duty engine for complex stateful stream processing (windowed aggregations, exactly-once across systems, huge scale).

```sql
-- ksqlDB: real-time aggregation
CREATE TABLE revenue_per_minute AS
  SELECT WINDOWSTART AS minute, SUM(amount) AS revenue
  FROM order_events
  WINDOW TUMBLING (SIZE 1 MINUTE)
  GROUP BY 1;
```

## When streaming is actually worth it

✅ Reach for it when:
- You need sub-second to few-second freshness (fraud detection, real-time dashboards, [OCS-style charging](../telecom/billing/ocs.md)).
- Multiple independent consumers need the same events (event-driven microservices).
- You need to decouple producers and consumers in time (a slow consumer doesn't block a fast producer).

⚠️ Skip it when:
- Nightly/hourly freshness is genuinely fine — batch ELT is simpler to build, run, and debug.
- Your team has no streaming ops experience yet — Kafka has a real operational learning curve; don't adopt it just because it's trendy.

## Where this connects

Kafka underpins event-driven microservices ([spring-microservices](../backend/microservices/spring-microservices.md) touches this), real-time analytics, and — very concretely — [telecom mediation](../telecom/billing/mediation.md), which is a textbook high-throughput streaming pipeline with hard correctness requirements.
