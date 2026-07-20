# 2. A ShedLock-guarded poller for scheduled posts, not Quartz

Status: accepted

## Context

Posts can be scheduled for a future time. Something has to notice when they come due and publish
them, and it has to keep working if the app ever runs as more than one instance without double-
posting.

Quartz is the obvious heavyweight answer: a full scheduler with its own tables, triggers and
clustering. The lighter answer is a plain `@Scheduled` poller that queries for due rows, made
single-execution with ShedLock.

## Decision

Poll. A `@Scheduled(fixedDelay=30s)` method calls `claimDue`, which runs a
`SELECT ... FOR UPDATE SKIP LOCKED` to grab due publications and flip them to `PUBLISHING` in one
transaction. `@SchedulerLock` (ShedLock, backed by a small `shedlock` table) makes sure only one
instance runs the poll at a time. So there are two independent guards against duplication: the row
lock and the scheduler lock.

## Consequences

No second scheduling engine, no Quartz tables or trigger model to learn — the "schedule" is just a
timestamp column and a query. It's not cron-expression scheduling and it's not sub-second precise;
due posts go out within one poll interval, which is exactly what this needs. If requirements later
grow into recurring schedules or fan-out at scale, that's when Quartz (or an external scheduler)
earns its keep. Until then this is the smaller thing that works.
