# Indexing — Deep Dive

[SQL essentials](sql-essentials.md) covers the basics. This is the part that separates "I added an index" from "I know why the query is still slow."

## B-tree — the default, and why it's the default

Almost every general-purpose index (PostgreSQL, MySQL InnoDB, SQL Server default) is a **B-tree**: a balanced, sorted tree structure.

```
                [50]
              /      \
          [20,35]    [70,90]
          /  |  \    /  |  \
       ... ... ... ... ... ...
```
- Lookups, range scans (`>`, `<`, `BETWEEN`), and sorting (`ORDER BY`) are all fast — `O(log n)` to find a starting point, then a sequential scan of the sorted leaves.
- This is *why* B-trees dominate: one structure handles equality, ranges, and sort, which covers most real queries.

## Other index types (know when the default isn't right)

| Type | Good for | Example |
|---|---|---|
| **Hash** | Pure equality lookups, nothing else | `WHERE id = ?` only — can't range-scan |
| **GIN** (Postgres) | Full-text search, JSONB, arrays | `WHERE tags @> ARRAY['urgent']` |
| **GiST** (Postgres) | Geometric/range types, nearest-neighbor | `WHERE location <-> point(...)` |
| **BRIN** (Postgres) | Huge tables, naturally ordered data (timestamps) | Tiny index, works when data correlates with physical row order |
| **Full-text index** (MySQL) | Text search without a separate search engine | `MATCH(body) AGAINST('query')` |

Reach for a specialized index type when your query pattern doesn't fit "equality/range on scalar columns" — forcing everything through a B-tree wastes their strengths.

## Composite indexes — the leftmost-prefix rule, properly explained

```sql
CREATE INDEX idx ON orders (customer_id, status, created_at);
```
This index can serve:
- `WHERE customer_id = ?` ✅ (uses the first column)
- `WHERE customer_id = ? AND status = ?` ✅ (uses first two)
- `WHERE customer_id = ? AND status = ? ORDER BY created_at` ✅ (all three, plus sort for free)
- `WHERE status = ?` ❌ (skips the leftmost column — the index can't help)
- `WHERE customer_id = ? AND created_at > ?` ⚠️ (uses `customer_id`, but `status` gap means `created_at` isn't used for range narrowing — still helps, just not fully)

**Column order = which queries the index actually serves.** Put your most selective / most-commonly-filtered column first, unless a specific query pattern demands otherwise.

## Covering indexes — skip the table entirely

```sql
CREATE INDEX idx_covering ON orders (customer_id, status) INCLUDE (total, created_at);

-- This query never touches the table — everything it needs is in the index
SELECT status, total, created_at FROM orders WHERE customer_id = 42;
```
When the index contains every column a query needs, the engine never reads the actual table row (no "heap fetch"). Huge win on hot, narrow queries — the classic "why is this simple query so fast" answer.

## What kills index usage (the silent performance killers)

```sql
-- Function on the indexed column → index ignored
WHERE LOWER(email) = 'a@b.com'          -- needs an index ON LOWER(email)

-- Implicit type coercion → index ignored
WHERE phone_number = 12345               -- phone_number is a VARCHAR; forces a cast

-- Leading wildcard → can't use a B-tree efficiently
WHERE name LIKE '%smith'                 -- vs 'smith%' which CAN use the index

-- OR across different columns → often can't use a single index
WHERE customer_id = 1 OR status = 'open' -- consider UNION of two indexed queries instead
```

## Reading EXPLAIN like it matters (because it does)

```sql
EXPLAIN ANALYZE SELECT * FROM orders WHERE customer_id = 42;
```
What to actually look for:
- **Seq Scan** on a large table where you expected an index → the index isn't being used; find out why (see above).
- **Estimated rows vastly different from actual rows** → statistics are stale; run `ANALYZE`.
- **Nested Loop over a huge outer set** → often means a missing index on the join column.
- **Sort** taking real time before a `LIMIT` → an index matching your `ORDER BY` could remove it entirely.

## The cost side nobody mentions enough

Every index **slows down writes** (insert/update/delete must maintain it too) and **takes disk space**. An over-indexed table with 15 rarely-used indexes on a write-heavy table is a real, common performance bug — not just "more indexes = more speed." Index the queries you actually run, drop the ones you don't (check `pg_stat_user_indexes` for usage counts before adding yet another one "just in case").
