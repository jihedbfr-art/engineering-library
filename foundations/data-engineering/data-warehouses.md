# Data Warehouses, Lakes & Lakehouses

## The three storage patterns

| | **Warehouse** | **Data Lake** | **Lakehouse** |
|---|---|---|---|
| Data shape | Structured, schema enforced | Any (structured, semi, raw files) | Any, but with table structure on top |
| Query | Fast SQL, optimized for analytics | Needs a compute engine on top | Fast SQL *and* raw flexibility |
| Cost | Higher per TB, optimized | Cheap object storage | Cheap storage + warehouse-like performance |
| Examples | Snowflake, BigQuery, Redshift | S3/GCS/ADLS + raw files | Databricks (Delta Lake), Snowflake w/ Iceberg |

The lakehouse pattern won the architecture debate of the last few years: **cheap object storage** (S3-style) as the base, with a **transactional table format** (Delta Lake, Apache Iceberg, Apache Hudi) layered on top giving you ACID guarantees, schema evolution, and time travel — warehouse ergonomics on lake economics.

## Star schema — the classic analytics modeling pattern

```
                 ┌──────────────┐
                 │  dim_customer │
                 └───────┬──────┘
┌──────────────┐        │        ┌──────────────┐
│  dim_product  │───┬────┼────┬───│   dim_date    │
└──────────────┘   │    │    │   └──────────────┘
                    │  ┌─▼────▼─┐
                    └─►│fct_sales│  (one row per sale, foreign keys to dims, + measures)
                       └─────────┘
```

- **Fact table**: the events/measurements (sales, calls, page views) — usually huge, numeric-heavy.
- **Dimension tables**: the context to slice by (customer, product, date, region) — smaller, descriptive.

Why bother: `SELECT sum(revenue) FROM fct_sales JOIN dim_date ... WHERE dim_date.quarter = 'Q3'` is simple and fast because the shape matches how analysts actually ask questions. Compare to querying raw OLTP tables designed for transactional apps, not analytics — usually a much worse experience.

## OLTP vs OLAP — the split that explains why warehouses exist at all

| | **OLTP** (app database) | **OLAP** (warehouse) |
|---|---|---|
| Optimized for | Many small reads/writes | Few, huge aggregate reads |
| Row vs columnar storage | Row-oriented | **Columnar** — scan only the columns a query needs |
| Example query | "Get order #4821" | "Sum revenue by region for 2025" |
| Tech | PostgreSQL, MySQL | Snowflake, BigQuery, Redshift |

Columnar storage is *the* reason warehouses crush analytical queries: summing one column across a billion rows only touches that column's data on disk, not entire rows. Running heavy analytics directly on your production OLTP database is a classic way to degrade the app that actually needs to be fast.

## Modeling layers (the dbt convention, but the idea predates the tool)

```
raw/source  →  staging  →  intermediate  →  marts
(as-is)        (cleaned,     (joins,          (business-ready,
                renamed,      reusable          one grain per
                typed)        logic)            table, documented)
```
Each layer has one job. Resist the temptation to write one giant query straight from raw tables to a dashboard — you'll rewrite the same join five times and nobody will trust the numbers because they diverge slightly each time.

## Partitioning & clustering — how warehouses stay fast at scale

```sql
-- BigQuery: partition by date, cluster by customer_id
CREATE TABLE fct_orders
PARTITION BY DATE(created_at)
CLUSTER BY customer_id
AS SELECT ...;
```
A query filtering `WHERE created_at = '2026-07-15'` only scans that day's partition — not the whole table's history. This is the warehouse equivalent of a database [index](../databases/sql-essentials.md) — get it wrong and costs/latency blow up quietly until someone notices the bill.

## Where this connects

Telecom [revenue assurance](../telecom/billing/revenue-assurance.md) reconciles data across many source systems — that's a warehouse/lakehouse modeling problem at its core: land everything, model into facts and dimensions, then query for discrepancies.
