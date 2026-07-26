---
name: data-pipeline-reviewer
description: Review an ETL/ELT pipeline or batch job for idempotency, backfill safety, and schema-drift handling — the failure modes that show up as silently wrong numbers weeks later, not a crash the same day. Use when asked to "review this pipeline", "check this data job before it runs on prod", or when a pipeline is about to start writing to a table other systems depend on.
---

# Data pipeline reviewer

A buggy pipeline rarely crashes loudly. It usually just produces numbers that are wrong in a way
nobody notices until a dashboard looks off weeks later and the root cause is three transformations
back. This skill looks for the specific failure modes that produce *silently wrong* output, not
just exceptions.

## What to check

1. **Idempotency — can this job run twice on the same input without corrupting output?** A job
   that appends rows on every run will double-count on a retry; a job using `INSERT` without an
   upsert key will fail loudly on retry (annoying but safe) or duplicate silently (much worse).
   The correct answer depends on the job, but the plan has to have one — check whether reruns are
   handled by `MERGE`/upsert-on-key, a delete-then-insert of the affected partition, or truncation
   of a staging table before load, not left to "it probably won't run twice."
2. **Backfill safety.** If this job needs to reprocess a month of historical data (a common,
   predictable need — a bug is found, a definition changes), can it actually do that without
   manual surgery? A job hardcoded to `WHERE date = CURRENT_DATE - 1` can't backfill without code
   changes; a job parameterized by date range can. Flag any pipeline where "how would we reprocess
   last month" doesn't have an obvious answer.
3. **Schema drift from the source.** What happens when an upstream source adds a column, renames
   one, or changes a type (`int` to `bigint`, nullable to non-nullable)? Does the pipeline fail
   loudly (acceptable), silently drop the new data (bad), or silently coerce/truncate a changed
   type (worse — this is how a truncated ID or a rounded decimal gets into a report and nobody
   catches it for months)?
4. **Partial-failure semantics for multi-step transformations.** If step 3 of 5 fails, does step 1
   and 2's output get rolled back, or does downstream now see a half-transformed dataset as if it
   were complete? A pipeline with no transactional boundary around a multi-step transform needs an
   explicit answer to this, not an assumption that failures are rare enough to ignore.
5. **Late-arriving and out-of-order data.** For any pipeline windowing by event time (not
   processing time), what happens to a record that arrives after its window already closed and was
   already aggregated? Silently dropped, silently double-counted in the next window, or handled
   with an explicit late-data policy (a grace period, a correction/backfill mechanism)?
6. **Data quality checks are actually gating, not just logging.** A row-count check or a null-rate
   check that logs a warning but still lets bad data flow downstream isn't a data quality gate,
   it's a data quality *report* — check whether a check that fails actually stops the pipeline or
   just gets noted somewhere nobody reads until asked.

## What NOT to flag

- Don't demand exactly-once semantics everywhere — at-least-once with a downstream dedup key is a
  legitimate, often simpler design; the review question is whether *some* correct idempotency
  strategy exists, not which specific one.
- Don't treat every schema addition (a new nullable column) as drift worth blocking — the concern
  is changes that break or silently corrupt existing transformations, not all change.

## Output shape

Group findings by which failure mode they represent (idempotency, backfill, schema drift, partial
failure, late data, gating), since the fix for each category tends to be structurally similar
across an otherwise unrelated set of findings.
