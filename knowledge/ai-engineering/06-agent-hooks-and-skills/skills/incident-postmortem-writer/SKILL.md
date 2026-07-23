---
name: incident-postmortem-writer
description: Turn raw incident notes, logs, and a timeline into a structured, blameless postmortem — root cause, impact, timeline, and prevention actions. Use when asked to "write a postmortem", "document this incident", or after an outage/production bug has been diagnosed and needs to be written up for the team.
---

# Blameless postmortem writer

A postmortem's job is to make the same failure impossible to repeat, not to assign blame. That
constraint shapes every section below — language that names a person as the cause belongs
nowhere in the output; language that names a *system condition* that allowed the failure belongs
everywhere.

## Structure

1. **One-sentence summary.** What broke, for how long, who/what was affected — written so
   someone who wasn't in the incident understands the stakes immediately, no scrolling required.

2. **Impact.** Concrete and measured, not vague: duration, request/user count affected if known,
   which systems degraded vs. fully failed. "Some errors" is not impact; "12% of checkout
   requests failed for 23 minutes" is.

3. **Timeline.** Timestamped, factual, one line per event: when the triggering change happened,
   when symptoms started, when it was detected, when mitigation began, when it resolved. Include
   the gap between "symptoms started" and "detected" explicitly — that gap is usually the
   highest-leverage thing to shrink next time, and it's easy to gloss over if the timeline only
   starts at detection.

4. **Root cause.** The actual mechanism, traced to a specific line of reasoning or code — not
   "a bug in the payment service" but "a retry loop with no backoff amplified load on an already
   degraded database connection pool during a routine failover." Distinguish root cause from
   contributing factors (things that made it worse or slower to detect, but didn't cause it).

5. **What went well.** Real postmortems aren't only failure lists — note what worked (an alert
   that fired correctly, a runbook that was accurate, a rollback that went cleanly) so it doesn't
   get accidentally removed in a future "improvement."

6. **Prevention actions.** Each one specific, owned, and — critically — distinguishing a fix
   that eliminates the failure mode structurally from one that only makes it less likely or
   faster to catch. Both are valid, but label them differently: "eliminates the failure mode" vs.
   "reduces detection time" vs. "reduces likelihood." A postmortem that only lists monitoring
   improvements without a single structural fix usually means the root cause wasn't actually
   addressed.

## Language rules (this is where "blameless" actually gets enforced)

- Never write "X forgot to..." or "X should have..." — rewrite as a system condition: "the
  deploy process didn't require a migration compatibility check" instead of "the engineer
  didn't check migration compatibility."
- Passive voice is fine here, unusually — "the config was not validated before deploy" is better
  postmortem language than naming who deployed it, precisely because the fix is a process gap,
  not a person.
- If the notes provided do name a person as the cause, rewrite around the system gap that let a
  human mistake become a production incident — a human being fallible is not itself the root
  cause of anything; the missing guardrail is.

## What NOT to include

- Speculation presented as fact — if the root cause isn't fully confirmed from the notes
  provided, say so explicitly ("most likely cause, not yet confirmed: ...") rather than writing
  it with false certainty.
- Blame, even softened ("unfortunately X happened to be the one who...") — cut it entirely.
