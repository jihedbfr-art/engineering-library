# Pipelines: ETL vs ELT, Batch, Orchestration

## ETL vs ELT — where the "transform" happens

```
ETL:  Extract → Transform (external engine) → Load     (transform BEFORE it hits the warehouse)
ELT:  Extract → Load (raw) → Transform (in-warehouse)   (transform AFTER, using the warehouse's own compute)
```

| | ETL | ELT |
|---|---|---|
| Transform runs on | A separate processing engine | The warehouse itself (SQL) |
| Raw data kept? | Often not | Yes — you can always re-derive |
| Modern default | Legacy pattern, still valid for some cases | **The default for cloud warehouses** (Snowflake/BigQuery/Databricks have compute to spare) |
| Tooling | Informatica, Talend, custom Spark jobs | Fivetran/Airbyte (extract+load) + dbt (transform) |

ELT won for most cases because cloud warehouses got cheap and powerful enough that "load raw, transform with SQL, keep everything" beats designing a rigid transform pipeline upfront. You get to change your mind about the transformation without re-extracting.

## Batch pipeline anatomy

```
Source systems ──► Extract ──► (raw landing zone) ──► Transform ──► Serving tables ──► BI/ML/apps
   (DBs, APIs,         │
    files, events)  scheduled or triggered
```

A concrete example (dbt-style ELT):
```sql
-- models/staging/stg_orders.sql — clean, rename, cast
select
  order_id,
  cast(created_at as timestamp) as created_at,
  customer_id,
  amount_cents / 100.0 as amount
from {{ source('raw', 'orders') }}
where order_id is not null

-- models/marts/fct_daily_revenue.sql — business logic, built on staging
select date_trunc('day', created_at) as day, sum(amount) as revenue
from {{ ref('stg_orders') }}
group by 1
```
Notice the layering: **staging** (clean 1:1 with source) → **marts** (business logic, joins, aggregates). Don't let raw source quirks leak into the tables analysts actually query.

## Orchestration — because pipelines have dependencies

You need something to run "transform daily revenue" only *after* "load orders" succeeds, retry on failure, alert on breakage, and show you the DAG.

```python
# Airflow-style DAG definition (conceptually similar in Dagster/Prefect too)
with DAG("daily_revenue", schedule="@daily") as dag:
    extract = PythonOperator(task_id="extract_orders", python_callable=extract_orders)
    load = PythonOperator(task_id="load_orders", python_callable=load_orders)
    transform = BashOperator(task_id="dbt_run", bash_command="dbt run --select fct_daily_revenue")
    extract >> load >> transform     # dependency order
```

| Tool | Style |
|---|---|
| **Airflow** | Python DAGs, the long-standing default, huge ecosystem |
| **Dagster** | Asset-centric (models data assets, not just tasks), strong typing/testing story |
| **Prefect** | Python-native, less boilerplate, dynamic workflows |
| **dbt** | Not an orchestrator itself — the transform layer, usually triggered *by* one of the above |

## Idempotency — the property that saves you at 3am

A pipeline WILL be re-run (retry after failure, backfill, manual rerun). Design every step so running it twice with the same input produces the same result, not duplicated data.

```sql
-- Bad: naive INSERT — reruns duplicate everything
INSERT INTO fct_daily_revenue SELECT ...;

-- Good: idempotent — safe to rerun
DELETE FROM fct_daily_revenue WHERE day = '{{ ds }}';
INSERT INTO fct_daily_revenue SELECT ... WHERE day = '{{ ds }}';
-- or: MERGE / INSERT ... ON CONFLICT DO UPDATE
```
This is the exact same discipline as [telecom mediation's deduplication](../telecom/billing/mediation.md) — reprocessing must not double-count.

## Incremental vs full refresh

- **Full refresh**: rebuild the whole table every run. Simple, correct, doesn't scale forever.
- **Incremental**: only process new/changed data since last run. Scales, but adds complexity (how do you detect "changed"? late-arriving data?).

Start full-refresh. Move to incremental only when the data volume actually forces it — premature incremental logic is a classic source of silent data gaps.
