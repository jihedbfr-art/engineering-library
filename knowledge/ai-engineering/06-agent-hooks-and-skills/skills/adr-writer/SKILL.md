---
name: adr-writer
description: Write an Architecture Decision Record (ADR) documenting a technical decision, the alternatives considered, and the tradeoffs accepted. Use when asked to "document this decision", "write an ADR", or after a significant technical choice (framework, database, architecture pattern) has been made and needs a durable record of why.
---

# Architecture Decision Record writer

An ADR's purpose is to answer, a year from now, "why did we do it this way?" without anyone
having to remember or reconstruct the conversation. That means the *rejected* alternatives and
*why* they were rejected matter as much as the chosen option — a record with only the winner
looks obvious in hindsight and teaches nothing about the actual tradeoff that was weighed.

## Structure

```
# ADR-NNNN: <short, specific title — "Use PostgreSQL for primary storage",
             not "Database decision">

## Status
Proposed | Accepted | Superseded by ADR-XXXX | Deprecated

## Context
What forced this decision? What constraint, requirement, or problem made the status quo
insufficient? Written so someone unfamiliar with the situation understands why a decision was
needed at all, not just what was decided.

## Decision
The choice, stated plainly in one or two sentences. No hedging — a decision record that reads
like it's still undecided has failed its one job.

## Alternatives considered
For each real alternative (not strawmen): what it was, why it was seriously considered, and the
specific reason it lost — not "it wasn't as good" but the concrete factor (cost, team
familiarity, a hard requirement it couldn't meet, an operational risk).

## Consequences
What this decision makes easier, and what it makes harder or forecloses. Include the honest
downsides being accepted, not just the benefits — a decision that reads as having zero
tradeoffs either had no real alternative worth considering, or the tradeoffs weren't looked at
hard enough.
```

## What makes the Alternatives section actually useful

The single biggest failure mode in ADRs: listing alternatives that were never realistically in
contention, so the "decision" looks pre-determined. If notes/context provided only describe one
option seriously, say so honestly in the Alternatives section ("no serious alternative was
evaluated because X constraint eliminated other options upfront") rather than inventing a
balanced-looking comparison that didn't actually happen.

## What NOT to do

- Don't write Consequences as purely positive — every real decision trades something away; if
  none is apparent from the input, ask what was given up rather than omitting the section's
  substance.
- Don't bury the actual decision in qualifications — Context is where nuance and constraints
  belong, Decision should be unambiguous.
- Don't retroactively make a rushed or constrained decision look like the result of a thorough
  evaluation it didn't get — if the real context was "we had one week and one viable option,"
  that's a legitimate ADR too, more useful honestly stated than dressed up.
