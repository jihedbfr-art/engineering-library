# 🗄️ Databases

Where your data lives — get this right and everything above it gets easier.

- [sql-essentials.md](sql-essentials.md) — joins, indexes, transactions, N+1, migrations
- [indexing-deep-dive.md](indexing-deep-dive.md) — B-trees, composite/covering indexes, reading EXPLAIN, what silently kills index usage
- [transactions-concurrency.md](transactions-concurrency.md) — isolation levels with real failure examples, locking, race conditions, deadlocks
- [nosql.md](nosql.md) — document, key-value, wide-column, graph — and when to use each
- [wide-column-cassandra.md](wide-column-cassandra.md) — designing tables per query, partition/clustering keys
- [graph-databases.md](graph-databases.md) — Cypher, when traversal beats joins, fraud/recommendation use cases
- [data-modeling.md](data-modeling.md) — entities, relationships, normalization, keys

Quick references: [SQL cheatsheet](../resources/cheatsheets/sql.md).

## The one rule

**SQL by default.** Reach for NoSQL when you have a specific, named reason (scale, schema flexibility, sub-ms lookups, graph traversal). Most applications are relational — and most "we need NoSQL" decisions are premature.
