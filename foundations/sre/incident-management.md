# Incident Management

When something breaks in production, the difference between a 10-minute blip and a 3-hour outage is almost always **process**, not raw technical skill. This is the process worth having in place before you need it — which is exactly when nobody wants to design it.

## Severity levels — decide the scale BEFORE the incident, not during it

```
SEV1  Critical — full outage, or a severe security/data-loss event.
      Page everyone relevant NOW, exec visibility, all-hands-on-deck.

SEV2  Major — significant degradation, a large subset of users affected.
      Page the on-call team, no need to wake the whole company.

SEV3  Minor — limited impact, workaround exists, can wait for business hours.

SEV4  Cosmetic — no real user impact, fix on the normal backlog.
```
The value of pre-agreed severity levels: in the middle of an actual incident, arguing about how bad it is wastes exactly the time you don't have. Assign severity fast using the pre-agreed criteria, and *move*.

## The incident lifecycle

```
Detect → Triage (severity + assign incident commander) → Mitigate (stop the bleeding)
       → Resolve (actual fix) → Postmortem (why, and what changes as a result)
```
**Mitigate before you fully understand root cause.** Roll back the last deploy, fail over to a healthy region, flip a feature flag off — restore service first, understand deeply afterward with the pressure off. Chasing full root-cause understanding while users are actively affected is usually the wrong tradeoff, and it's an easy trap to fall into when the fix feels "almost found."

## The Incident Commander role

One person owns coordination — not necessarily the person fixing it. Their job: keep a timeline, decide who's needed, communicate status outward on a set cadence, and explicitly make the call between "mitigate now" and "understand more first." Without this role, everyone competent starts independently debugging the same problem, stepping on each other, and nobody communicates status until the postmortem — a room full of very capable people can be less effective than three the moment there's no explicit coordinator among them.

## Communication during an incident — a real discipline, not busywork

```
Every N minutes (severity-dependent), post a status update:
- What's known so far
- Current impact (who/what's affected)
- What's being done right now
- Next update time
```
Silence during an incident is worse than an unglamorous "still investigating" update — it reads as "nobody's working on this" even when three engineers are deep in it, and it's exactly what erodes stakeholder trust fastest.

## Blameless postmortems — the template that actually produces change

```markdown
## Summary
One paragraph: what happened, impact, duration.

## Timeline
[14:02] Alert fired: error rate > 5%
[14:05] On-call acknowledged, began investigating
[14:12] Identified: new deploy introduced a connection pool leak
[14:15] Rolled back deploy
[14:18] Error rate back to normal
[14:45] Root cause confirmed: pool size misconfigured for new replica count

## Root cause
The actual technical/process cause — not "engineer X made a mistake,"
but "our config validation didn't catch an invalid pool size at deploy time."

## Impact
Users affected, duration, business impact (revenue, SLA, trust).

## What went well
Genuinely note this — fast detection, good mitigation, whatever worked.

## Action items
| Action | Owner | Due |
|---|---|---|
| Add config validation for pool size at deploy time | @name | date |
| Add alert for connection pool saturation, not just error rate | @name | date |

(Every action item gets a real owner and a real date, or it never happens —
"we should improve monitoring" with no owner is not an action item, it's a wish.)
```

## The trap of "human error" as a root cause

"An engineer ran the wrong command" is never the actual root cause — it's the *proximate* trigger. The real root cause is almost always **why the system made that mistake easy to make and hard to catch**: no confirmation step on a destructive command, no staging environment that would have caught it, no monitoring that flagged the anomaly quickly. Fix the system that allowed a human mistake to reach production undetected, not just the human — the same mistake will happen again, from someone else, if the system itself doesn't change.

## Where this connects

Incidents burn [error budget](sre-fundamentals.md) — that's the direct link between this page and SLOs. Good [observability](../devsecops/monitoring/observability.md) is what makes "Detect" and "Triage" fast instead of "wait for a customer complaint." [Deployment practices](../devsecops/README.md) (canary releases, feature flags, fast rollback) are what make "Mitigate" a 2-minute action instead of a 45-minute scramble.
