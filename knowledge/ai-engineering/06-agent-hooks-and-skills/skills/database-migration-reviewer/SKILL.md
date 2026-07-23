---
name: database-migration-reviewer
description: Review a database schema migration for rolling-deploy safety, lock duration on large tables, and reversibility. Use when reviewing a Flyway/Liquibase/Alembic migration file, a schema change PR, or when asked to "review this migration" or "check this schema change is safe".
---

# Database migration review

A migration that's correct in isolation can still take production down if it's incompatible with
the old application version still running during a rolling deploy, or if it locks a large table
for longer than anyone tested against a realistic row count.

## What to check, in priority order

1. **Rolling-deploy compatibility.** During a rolling deploy, old and new application code run
   simultaneously against the same schema for a window of time. A migration that drops or renames
   a column/table the old code still reads/writes will break that old code mid-rollout. Flag any
   destructive schema change (`DROP COLUMN`, `RENAME`, type change) that isn't split into an
   expand step (add the new, keep the old working) and a separate later contract step (remove the
   old, only after the rollout is confirmed complete).

2. **Lock duration on the actual table size.** `ALTER TABLE ... ADD COLUMN` with a default value,
   an index creation without `CONCURRENTLY` (PostgreSQL) or online DDL support, or a full table
   rewrite locks the table for the operation's duration — fine on a table with 10 rows in a test
   database, potentially minutes of blocked writes on a production table with tens of millions.
   Flag any schema change that doesn't specify whether it was evaluated against real production
   table size, and any index creation not using an online/concurrent method where the database
   supports one.

3. **Reversibility.** Can this migration's down/rollback script actually restore the prior state,
   or does the "down" migration silently lose data (e.g., a `DROP COLUMN` down-migration can't
   restore the dropped column's data)? Flag any migration whose rollback path isn't genuinely
   safe to run, and say so explicitly rather than leaving it implied.

4. **Default values and backfills on large tables.** Adding a `NOT NULL` column without a default
   forces every existing row to be rewritten in some database engines; even with a default,
   backfilling millions of rows in the same migration transaction can hold locks far longer than
   expected. Flag any `NOT NULL` addition or bulk backfill that isn't broken into batches or a
   background job for large tables.

5. **Foreign key and constraint additions on existing data.** Adding a constraint validates all
   existing rows by default in many engines — flag whether that validation was accounted for
   (`NOT VALID` + separate `VALIDATE CONSTRAINT` in PostgreSQL, or the engine's equivalent
   two-step approach) on any table large enough for that validation pass to matter.

## How to report findings

Classify each finding as **blocks the rolling deploy** (will break running old code), **risks a
production lock/outage** (works but could stall on real data volume), or **loses data on
rollback** — lead with the first category, it's the one most likely to cause a live incident
during the very deploy that ships the migration.

## What NOT to flag

- Purely additive, backward-compatible changes on small/low-traffic tables (`ADD COLUMN
  nullable_field TEXT` with no default) — low risk by construction, don't need the full checklist.
- Migrations already explicitly scoped as "requires downtime, scheduled maintenance window" — the
  rolling-deploy compatibility concern doesn't apply if there's no rolling deploy happening.
