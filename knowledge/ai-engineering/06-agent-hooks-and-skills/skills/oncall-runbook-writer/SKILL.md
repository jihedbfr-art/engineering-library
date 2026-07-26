---
name: oncall-runbook-writer
description: Write an on-call runbook for a service — the alerts it can fire, what each one actually means, and the first three diagnostic steps for each. Use when asked to "write a runbook for this service", "document what on-call should do when X pages", or before a new service goes on an on-call rotation without anyone having written down how to respond to its alerts.
---

# On-call runbook writer

A runbook's only real reader is someone half-awake at 3am who has never touched this service
before and has minutes, not hours, to figure out what's happening. That reader doesn't need
architecture background or design rationale — they need "this alert means X, check Y first, here's
how to tell if it's Z or something else." This skill writes for that reader specifically, not for
someone doing a leisurely read during business hours.

## Structure, per alert

```
## Alert: <exact alert name as it appears in the paging tool>

**What it means:** One sentence, plain language, no jargon a first responder from another team
might not know. Not "elevated p99 latency on the ingestion path" — "requests to the ingestion API
are taking too long, which usually means downstream is backed up."

**User impact:** What a user or customer actually experiences right now, concretely — this is
what determines urgency and whether to escalate immediately versus investigate calmly first.

**First three checks, in order:**
1. The single fastest thing to check that narrows down the cause (a specific dashboard, a specific
   log query, a specific health-check endpoint) — not "check the dashboards," a named one with a
   URL or exact path.
2. The second check, conditioned on what the first one showed.
3. The third — by this point the runbook should point at either a known fix or a clear escalation
   path, not a fourth open-ended "investigate further."

**Known causes and their fixes:** A short table of "if you see X in the logs/dashboard, it's
usually caused by Y, fix is Z" — built from real past incidents where possible, not hypothetical
causes invented to fill the section.

**When to escalate, and to whom:** The specific condition under which this stops being a
first-responder problem (a dependency team needs to be paged, a decision needs someone with more
authority) — stated as a threshold or symptom, not "if it seems bad."
```

## What separates a useful runbook from a useless one

The test: could someone who has never seen this service before follow it at 3am and make real
progress in the first five minutes? If a step assumes context the reader won't have (an internal
tool name with no link, an abbreviation never expanded, "check the usual place"), it fails that
test. Write every step as if handing it to the newest person on the rotation, because eventually it
will be.

## What NOT to do

- Don't write a runbook that's really an architecture document with alerts bolted on — cut
  anything that doesn't help someone respond to a page faster; a "why this service exists" section
  belongs in a README, not here.
- Don't invent known-cause entries with no real basis — an honest "no known-cause pattern yet,
  escalate to the owning team" is more useful than a fabricated plausible-sounding cause that sends
  someone down the wrong path during a real incident.
- Don't skip the "user impact" line even for an alert that seems purely internal (a queue depth
  threshold, a cache hit-rate drop) — someone deciding whether to wake up a second person needs to
  know if this is customer-visible right now or a leading indicator with time to spare.
