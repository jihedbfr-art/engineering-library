# Legacy Migration Playbook

The project discipline that actually determines success — independent of which pattern ([strangler fig](strangler-fig-pattern.md) or otherwise) you use technically. Drawn from real government/enterprise system migrations and telecom core migrations, where "we missed a requirement" isn't an inconvenience, it's a regulatory incident or a subscriber-facing outage.

## Phase 1: Requirement archaeology — the phase everyone underestimates

Before writing any new code, the actual first job is answering: **"what does the current system really do, in production, today"** — not what the original spec said, not what the outdated documentation claims, but what it *actually does*, including every quiet fix and undocumented rule accumulated over years.

```
Sources, roughly in order of reliability:
1. Production behavior itself — trace real transactions through the current system
2. The current codebase (PL/SQL packages, Java business logic) — the actual source of truth
3. Support tickets / bug history — reveals edge cases nobody thought to spec
4. Interviews with people who've operated the system for years — often the ONLY
   record of a rule that exists purely as tribal knowledge
5. Original documentation — useful for context, least trustworthy for "current truth"
   (it describes what the system was DESIGNED to do, not what it evolved into)
```
Concretely, for something like a tax-declaration ERP: every report a specific client requested over the years, every edge case in how declarations are validated, every quiet exception added to the payment-matching logic for one specific tax office's workflow — none of this lives in a clean spec anywhere. It lives in the PL/SQL, in a decade of support tickets, and in a few people's memory. **This phase is slow, unglamorous, and it's where migrations actually succeed or fail** — an under-budgeted requirement phase is the single most common root cause of a "successful" migration that turns out to be quietly missing a real business rule six months later.

## Phase 2: Prioritization — order by risk and value, not by ease

```
High risk, high value   → migrate with the most care, but don't migrate LAST
                           (the team's confidence and tooling should already
                            be proven on lower-risk modules by the time you
                            reach these — see the strangler fig ordering example)
Low risk, high value    → migrate FIRST — quick wins that build stakeholder trust
Low risk, low value     → migrate whenever convenient, or leave for last
High risk, low value    → seriously ask whether this module needs migrating at all
```

## Phase 3: Environment parity — the unglamorous prerequisite

Dev/UAT environments that have quietly drifted from production configuration are one of the most common, least discussed sources of "it worked in testing" failures. Before serious migration testing begins: confirm the test environment's data volume, configuration, and integration points genuinely resemble production — not just structurally, but in scale and edge-case coverage. A UAT database seeded with 200 clean sample records will not surface the same bugs as production's 15 years of real, messy data.

## Phase 4: Parallel running & shadow validation

```
1. New system built, but not yet live for real users
2. Replay real (anonymized) production transactions through BOTH systems
3. Diff the outputs — every mismatch is either:
     a) a bug in the new system, OR
     b) a business rule you didn't know the old system had
   Both outcomes are valuable. (b) is arguably more valuable — it's a
   requirement-archaeology gap caught before go-live instead of after.
4. Only proceed to real cutover once mismatches are understood and resolved
```
This step is what actually catches the requirement-archaeology gaps that inevitably survive Phase 1, however thorough it was — nobody's requirement gathering is ever perfectly complete, and shadow validation is the safety net that catches what it missed, before a real user does.

## Phase 5: Staged cutover, never big-bang

Migrate in controlled batches (by module, by region, by client segment — whatever segmentation the specific system allows), each batch validated before the next begins, with an explicit rollback plan **for that batch specifically** — not a vague "we'll figure it out" if something goes wrong. This is the direct parallel to [core network migration](../telecom/core-network-migration.md)'s batch-wave approach; the discipline transfers completely across domains because the underlying problem — validate incrementally, keep rollback scoped — is the same problem regardless of whether you're migrating a tax ERP or a telecom core.

## Phase 6: Reconciliation after every wave, not just at the end

After each migrated batch, actively cross-check: does the new system's output for this batch match what the old system would have produced? Waiting until the very end to check this converts what should be N small, cheap, easy-to-diagnose discrepancies into one large, tangled one that's far harder to trace back to its actual cause.

## The coordination reality nobody puts in the technical plan

Real migrations run on regular cross-team meetings — kickoffs, feasibility workshops, weekly syncs — coordinating the team building the new system, the people (often just one or two) who deeply understand the old system's undocumented behavior, business/client stakeholders validating that outputs are actually correct, and whoever operates production throughout. **The technical migration is maybe half the actual work.** The other half is making sure every one of those groups agrees, explicitly, on what "correct" means for each migrated piece — a disagreement surfaced in a workshop costs an hour; the same disagreement discovered in production costs an incident, or in a regulated system, potentially a compliance finding.

## The one-sentence version of this whole page

Assume the old system is right until proven otherwise, validate everything incrementally against real production behavior, and budget the "figuring out what it actually does" phase as if it's the hard part — because in nearly every real migration, it is.
