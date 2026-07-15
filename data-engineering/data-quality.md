# Data Quality — Catching Bad Data Before It Spreads

The unglamorous discipline that determines whether anyone trusts your pipelines. A dashboard with wrong numbers is worse than no dashboard — at least "no dashboard" doesn't lead someone to a bad decision with false confidence.

## Where bad data comes from

| Source | Example |
|---|---|
| **Upstream schema changes** | A source API silently renames a field; your pipeline reads nulls forever until someone notices |
| **Duplicate events** | A retry re-sends the same event; naive counting doubles it |
| **Late-arriving data** | An event for "yesterday" arrives today, after yesterday's aggregate already ran |
| **Logic bugs** | A join fan-out silently multiplies rows |
| **Data drift** | Values technically valid but statistically wrong (a currency field suddenly all zeros) |

## Data contracts — catch it at the source, not downstream

A **data contract** is an explicit agreement between the team producing data and the teams consuming it: schema, semantics, and guarantees, checked automatically.

```yaml
# Example contract for an event
event: order.created
fields:
  order_id: { type: string, required: true }
  amount_cents: { type: integer, required: true, min: 0 }
  currency: { type: string, enum: [EUR, USD, TND] }
breaking_change_policy: new fields optional; no field removal without major version
```
The point: a breaking change gets caught **at CI time on the producer's side**, not three weeks later when an analyst notices the dashboard looks weird. Same philosophy as an API contract, applied to data.

## Testing pipelines like you test code

```sql
-- dbt-style tests, run automatically on every pipeline run
-- models/schema.yml
models:
  - name: fct_orders
    columns:
      - name: order_id
        tests: [unique, not_null]
      - name: amount
        tests:
          - not_null
          - dbt_utils.accepted_range: { min_value: 0 }
      - name: customer_id
        tests:
          - relationships: { to: ref('dim_customer'), field: customer_id }
```
Four cheap tests (`unique`, `not_null`, range, referential integrity) catch a surprising share of real incidents. Add them by default to every table, not as an afterthought.

## The checks that matter most, roughly in order of value

1. **Freshness** — did the data actually update? (a silently-stale pipeline looks fine at a glance)
2. **Volume anomalies** — row count today vs the trailing average; a 90% drop or a 10x spike is almost always a bug, not reality
3. **Uniqueness / not-null** on keys — duplicate or missing primary keys break every downstream join
4. **Referential integrity** — foreign keys that should always resolve
5. **Distribution checks** — a percentage column suddenly averaging 150% is a unit bug, not a trend

## Observability for data (borrow from software observability)

```
Pipeline run → emit metrics (row counts, null rates, freshness, duration)
             → alert on anomalies (vs historical baseline, not just hard thresholds)
             → link the alert to the exact run + table + upstream source
```
Same principles as [application observability](../devsecops/monitoring/observability.md): you can't fix what you can't see, and alerting on raw thresholds without context creates the same alert fatigue as in any other system.

## Incident response for data (yes, this deserves one too)

1. **Quarantine** — stop the bad data from reaching consumers (pause the pipeline, or exclude the bad batch) before investigating.
2. **Assess blast radius** — which downstream tables/dashboards/models consumed the bad batch?
3. **Fix at the source** if possible, backfill correctly, communicate what was wrong to anyone who made a decision off bad numbers.
4. **Add a test** that would have caught it — every real incident becomes a permanent guard, same discipline as [LLM eval sets growing from failures](../ai/machine-learning/evals-and-testing.md).

## The uncomfortable truth

Nobody gets promoted for the incident that didn't happen. Data quality work is invisible when it's working and painfully visible when it's not — budget for it anyway; it's cheaper than the meeting where someone asks "why did we tell the board the wrong number."
