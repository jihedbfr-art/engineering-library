# Wide-Column Databases — Cassandra Deep Dive

[NoSQL overview](nosql.md) introduces the four families at a glance. Wide-column deserves more — its data model is genuinely unlike SQL, and using it like a relational database is the most common way to have a bad time with it.

## The core idea: design the table for the query, not the entity

In SQL you model entities and relationships, then write whatever query you need — the query adapts to the schema. In Cassandra, **you design a table per query pattern**, denormalizing aggressively. This is the single biggest mental shift.

```
SQL instinct:        one `orders` table, JOIN to get customer name when needed
Cassandra instinct:   a table `orders_by_customer` with the customer name
                       DUPLICATED into every row — no joins exist
```

## Partition key & clustering key — the two decisions that define performance

```sql
CREATE TABLE orders_by_customer (
  customer_id  uuid,
  order_date   timestamp,
  order_id     uuid,
  total        decimal,
  status       text,
  PRIMARY KEY ((customer_id), order_date, order_id)
);
--             └── partition key ──┘  └── clustering columns ──┘
```
- **Partition key** (`customer_id`): determines *which node(s)* hold the data. All rows sharing a partition key live together, sorted by the clustering columns — this is what makes `WHERE customer_id = ?` a single, fast, targeted read.
- **Clustering columns** (`order_date, order_id`): sort order *within* a partition.

**The rule that governs everything**: you can only efficiently query by the partition key (equality) and clustering columns (range/equality, in order). Query on anything else (`WHERE status = 'pending'` without a partition key) and Cassandra either refuses or scans the entire cluster — exactly what you don't want at scale.

## Consequence: one entity, many tables

```sql
-- Same order data, three tables, each shaped for one query pattern
CREATE TABLE orders_by_customer (customer_id, order_date, order_id, ... , PRIMARY KEY ((customer_id), order_date));
CREATE TABLE orders_by_status   (status, order_date, order_id, ... , PRIMARY KEY ((status), order_date));
CREATE TABLE orders_by_id       (order_id, ... , PRIMARY KEY (order_id));
```
Write once, fan out to every table your query patterns need. This feels wasteful coming from SQL — it's the deliberate trade Cassandra makes: **cheap, fast, linearly-scaling writes** in exchange for **query-shaped denormalized reads**. You pay in storage and write amplification, not in read latency at scale.

## Why it scales the way it does

- **No joins, no cross-partition transactions** by default → no coordination overhead → near-linear horizontal scaling by adding nodes.
- Data is automatically distributed and replicated across nodes via **consistent hashing** on the partition key.
- **Tunable consistency** per query: `CONSISTENCY QUORUM` (majority of replicas must agree) vs `ONE` (fastest, weakest guarantee) — you choose the latency/consistency trade per operation, not globally.

## When wide-column is genuinely the right call

✅ Time-series data (sensor readings, [telecom CDRs](../telecom/billing/mediation.md), event logs) — natural partition key (device/subscriber id), natural clustering key (timestamp).
✅ Massive write throughput, many more writes than complex reads.
✅ You know your query patterns upfront and they're stable.

⚠️ Skip it when:
- You need ad-hoc queries you can't predict in advance (analysts want to slice data every which way — that's a warehouse's job, not Cassandra's).
- You need multi-row ACID transactions or joins as a core requirement.
- Your team can't commit to the "design tables per query" discipline — half-applying it produces the worst of both worlds.

## The mistake almost everyone makes on their first Cassandra table

Modeling it like a normalized SQL schema — one table per entity, expecting to join at query time. Cassandra has no query-time join. If you catch yourself wanting one, you either need a different table (denormalized for that query) or a different database.
