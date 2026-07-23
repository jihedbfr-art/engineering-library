# NoSQL Databases

"NoSQL" = "not only SQL": databases that trade the relational model for scale, flexibility, or specific access patterns. Not a replacement for SQL — a different tool for different jobs.

## The four families

| Type | Model | Examples | Best for |
|---|---|---|---|
| **Document** | JSON-like documents | MongoDB, Couchbase | flexible/nested data, content, catalogs |
| **Key-value** | simple key → value | Redis, DynamoDB | caching, sessions, high-speed lookups |
| **Wide-column** | rows with dynamic columns | Cassandra, HBase | massive write throughput, time-series |
| **Graph** | nodes + edges | Neo4j, Neptune | relationships (social, fraud, recommendations) |

## Document (MongoDB) — the common one

```js
// A document — no fixed schema, nested structure
{
  _id: ObjectId("..."),
  title: "My note",
  tags: ["work", "urgent"],
  author: { name: "Ada", email: "ada@x.com" },
  comments: [ { text: "nice", at: ISODate("...") } ]
}

db.notes.find({ tags: "work" }).sort({ createdAt: -1 }).limit(10)
db.notes.updateOne({ _id: id }, { $push: { tags: "done" } })
```
✅ Flexible schema (fields vary per document), data that's read together stored together (fewer joins), maps naturally to objects.
⚠️ No enforced schema = your app must enforce consistency; duplicated data; multi-document transactions are possible but less natural than SQL.

## Key-value (Redis) — speed king

```
SET session:abc "userdata"  EX 3600     # with 1h expiry
GET session:abc
INCR page:views                          # atomic counter
LPUSH queue:jobs "job1"                   # lists, sets, sorted sets, hashes
```
In-memory → microsecond latency. Uses: cache, session store, rate limiting, leaderboards (sorted sets), pub/sub, job queues. Often sits *next to* your SQL DB, not instead of it.

## When to choose NoSQL over SQL

Choose NoSQL when you have a **specific reason**:
- Scale beyond one machine's writes (wide-column).
- Schema genuinely varies per record (document).
- Sub-millisecond key lookups / caching (key-value).
- Relationship-heavy traversals (graph).

Choose **SQL by default** when: you need transactions, relationships, strong consistency, or ad-hoc queries — which is most business applications. See [SQL essentials](sql-essentials.md).

## The consistency trade-off

Many NoSQL systems favor **availability and partition tolerance** with *eventual consistency* — a write may take time to appear everywhere. Great for "likes count" and feeds, dangerous for bank balances. Know your database's guarantees before trusting it with critical data → [CAP theorem](../computer-science/system-design.md).

## Polyglot persistence (the real-world answer)

Modern systems use several: **PostgreSQL** for core relational data, **Redis** for cache/sessions, **Elasticsearch** for full-text search, maybe **Cassandra** for event logs. Pick per workload — one database rarely fits everything.
