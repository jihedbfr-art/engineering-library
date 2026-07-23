# SQL Essentials — What Actually Matters

## Joins, visually

```
INNER JOIN    only matches            A ∩ B
LEFT JOIN     all of A + matches      A (+ B where exists)
FULL JOIN     everything              A ∪ B
```

```sql
-- notes with their author, including authorless drafts
SELECT n.title, u.name
FROM notes n
LEFT JOIN users u ON u.id = n.user_id;
```

## Indexes — the 80/20 of performance

```sql
CREATE INDEX idx_notes_user_created ON notes (user_id, created_at DESC);
```

- An index is a sorted structure: great for `WHERE`, `JOIN ... ON`, `ORDER BY` on indexed columns.
- **Composite index order matters**: `(user_id, created_at)` serves `WHERE user_id = ?` and `WHERE user_id = ? ORDER BY created_at`, but NOT `WHERE created_at > ?` alone.
- Functions kill index use: `WHERE LOWER(email) = ?` needs an index on `LOWER(email)`.
- Cost: every index slows writes. Index what queries need, not every column.

**The one habit**: `EXPLAIN ANALYZE` any query you're unsure about. `Seq Scan` on a big table in a hot path = fix it.

## Transactions & isolation

```sql
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;      -- both or neither (Atomicity)
```

| Level | Blocks | Default in |
|---|---|---|
| READ COMMITTED | dirty reads | PostgreSQL, Oracle |
| REPEATABLE READ | + non-repeatable reads | MySQL/InnoDB |
| SERIALIZABLE | + phantoms (retry on conflict!) | — |

Practical rules:
- Keep transactions **short** — no HTTP calls inside a transaction, ever.
- Race conditions on money/stock: `SELECT ... FOR UPDATE` or optimistic locking (version column), not hope.

## N+1 — the classic ORM trap

```
notes = findAll()             -- 1 query
for n in notes: n.user.name   -- N queries 😱
```
Fix: fetch join / eager load for that use case (`JOIN FETCH` in JPQL, `include` in most ORMs). Detect it: log SQL in dev and count.

## Modeling quick rules

1. Normalize until it hurts, denormalize where measured performance demands it.
2. Every table: primary key, `created_at`, `updated_at`.
3. FK constraints ON — data integrity in the DB, not just the app.
4. `NUMERIC/DECIMAL` for money. Never float. Never.
5. UTC timestamps (`timestamptz` in Postgres), convert at display.
6. Soft delete (`deleted_at`) only when business needs history — it complicates every query.

## Migrations

- Versioned, in git, run by CI (Flyway/Liquibase for Java, whatever your stack blesses).
- Forward-only mindset; write the rollback plan for the risky ones.
- Zero-downtime pattern for column changes: add new → dual-write → backfill → switch reads → drop old. Not: rename in place.
