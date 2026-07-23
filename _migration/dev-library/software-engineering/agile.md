# Agile & Team Practices

How modern software teams organize work. Agile is a **mindset** (ship small, get feedback, adapt) — the ceremonies are just tools serving it, not the point.

## The core idea

Instead of specifying everything upfront and building for a year (waterfall), deliver working software in short cycles, learn from real feedback, and adjust. Value working software and responding to change over rigid plans and documentation.

## Scrum — the most common framework

Work in fixed **sprints** (usually 2 weeks). Roles and events:

| Ceremony | When | Purpose |
|---|---|---|
| **Sprint planning** | start | pick what to build this sprint |
| **Daily standup** | daily, ~15 min | sync: done / doing / blockers |
| **Sprint review** | end | demo working software to stakeholders |
| **Retrospective** | end | improve *how* the team works |

Roles: **Product Owner** (what & priority), **Scrum Master** (process & unblocking), **Dev Team** (how & building).

## Kanban — continuous flow

No sprints. A board (To Do → In Progress → Review → Done) with **WIP limits** (max items per column). Pull work as capacity frees up. Great for support/ops and steady streams of varied work.

```
To Do   │ In Progress (max 3) │ Review │ Done
────────┼─────────────────────┼────────┼──────
 #12    │  #8   #9            │  #7    │ #5
 #13    │                     │        │ #6
```

## User stories

Frame work from the user's view:
> As a **[user]**, I want **[goal]** so that **[benefit]**.

Good stories are **INVEST**: Independent, Negotiable, Valuable, Estimable, Small, Testable. Each has **acceptance criteria** — the concrete "done" checklist.

## Estimation

- **Story points** — relative effort/complexity (often Fibonacci: 1,2,3,5,8), not hours. Teams estimate consistency, then track velocity (points/sprint).
- Don't weaponize velocity as a productivity metric across teams — it's a planning tool, not a scoreboard.

## What actually makes teams effective (beyond ceremonies)

1. **Short feedback loops** — small PRs, CI/CD, frequent demos. Ship to learn.
2. **Psychological safety** — people raise problems and admit mistakes without blame. The single biggest predictor of team performance.
3. **Definition of Done** — shared and enforced (code + tests + review + docs + deployed), so "done" isn't ambiguous.
4. **Blameless retrospectives** — fix the system, not the person → same spirit as [incident post-mortems](../devsecops/monitoring/observability.md).
5. **Limit work in progress** — finishing beats starting. Context-switching is the silent tax.

## The honest caveat

Agile fails when it becomes cargo-cult ceremony — standups that are status theater, "points" as surveillance, sprints that are just waterfall in 2-week chunks. Keep the mindset (small, feedback, adapt); drop rituals that stop serving it.
