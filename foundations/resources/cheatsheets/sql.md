# SQL Cheatsheet

Deep dive: [databases/sql-essentials.md](../../databases/sql-essentials.md)

## Query skeleton (logical execution order)

```sql
SELECT   col, AGG(x)          -- 5. pick & compute
FROM     t                    -- 1. source
JOIN     u ON u.id = t.uid    -- 1. combine
WHERE    cond                 -- 2. filter rows
GROUP BY col                  -- 3. bucket
HAVING   AGG(x) > 10          -- 4. filter buckets
ORDER BY col DESC             -- 6. sort
LIMIT    20 OFFSET 40;        -- 7. slice
```
Written top-down, executed roughly `FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY → LIMIT`. That's why you can't use a `SELECT` alias in `WHERE`.

## Joins

```sql
INNER JOIN   -- matching rows only
LEFT  JOIN   -- all left + matched right (NULLs where none)
RIGHT JOIN   -- all right + matched left
FULL  JOIN   -- everything
CROSS JOIN   -- cartesian product (careful)
SELF JOIN    -- table joined to itself (hierarchies)
```

## Aggregation

```sql
COUNT(*)  COUNT(DISTINCT x)  SUM(x)  AVG(x)  MIN/MAX(x)
STRING_AGG(name, ', ')            -- Postgres; GROUP_CONCAT in MySQL
FILTER (WHERE cond)               -- conditional aggregate (Postgres)
SELECT count(*) FILTER (WHERE status='shipped') AS shipped FROM orders;
```

## Window functions (aggregate without collapsing rows)

```sql
SELECT name, dept, salary,
  RANK()       OVER (PARTITION BY dept ORDER BY salary DESC) AS rnk,
  AVG(salary)  OVER (PARTITION BY dept)                      AS dept_avg,
  salary - LAG(salary) OVER (ORDER BY hired_at)              AS vs_prev
FROM employees;
```
`ROW_NUMBER` / `RANK` / `DENSE_RANK` · `LAG` / `LEAD` · running totals with `SUM() OVER (ORDER BY ...)`.

## CTEs & recursion

```sql
WITH recent AS (
  SELECT * FROM orders WHERE created_at > now() - interval '7 days'
)
SELECT customer_id, count(*) FROM recent GROUP BY customer_id;

-- Recursive: walk a tree
WITH RECURSIVE tree AS (
  SELECT id, parent_id, name FROM categories WHERE parent_id IS NULL
  UNION ALL
  SELECT c.id, c.parent_id, c.name
  FROM categories c JOIN tree t ON c.parent_id = t.id
)
SELECT * FROM tree;
```

## Upsert

```sql
-- Postgres
INSERT INTO tags (name) VALUES ('sql')
ON CONFLICT (name) DO UPDATE SET used = tags.used + 1;

-- MySQL
INSERT INTO tags (name) VALUES ('sql')
ON DUPLICATE KEY UPDATE used = used + 1;
```

## Performance one-liners

```sql
EXPLAIN ANALYZE SELECT ...;        -- read the plan; hunt Seq Scan on big tables
CREATE INDEX idx ON t (a, b);      -- composite: leftmost prefix rule applies
```
- Avoid `SELECT *` in code paths. Avoid functions on indexed columns in `WHERE`.
- `EXISTS` often beats `IN (subquery)`; `JOIN` often beats correlated subqueries.

## Date & null handling

```sql
COALESCE(x, 0)          -- first non-null
NULLIF(a, b)            -- NULL if a=b (avoid div-by-zero: x / NULLIF(y,0))
x IS DISTINCT FROM y    -- null-safe inequality (Postgres)
now(), current_date, date_trunc('month', ts), ts + interval '1 day'
```
