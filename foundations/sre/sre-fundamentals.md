# SRE Fundamentals

## SLI, SLO, SLA — the vocabulary that makes "reliable" measurable

"The system should be reliable" is a wish. These three terms turn it into a number you can actually manage against:

| Term | What it is | Example |
|---|---|---|
| **SLI** (Indicator) | A measured metric | "% of requests completing in < 300ms" |
| **SLO** (Objective) | Your internal target for that SLI | "99.9% of requests < 300ms, measured over 30 days" |
| **SLA** (Agreement) | A contractual promise to customers, usually looser than the SLO, with real penalties if missed | "99.5% uptime or we credit your invoice" |

The SLA should always be looser than the SLO — you want room to notice and fix a problem *before* you're in breach of a paid contract, not exactly at the edge of it.

## Error budgets — the idea that changes how teams actually behave

If your SLO is 99.9% over 30 days, you have an **error budget** of 0.1% — roughly 43 minutes of allowed downtime/degradation that month. This reframes the eternal "ship features vs. be careful" tension into a number both engineering and product can agree on:

```
Budget remaining, healthy month  → ship features, take reasonable risks, deploy freely
Budget nearly exhausted          → freeze non-critical changes, focus entirely on stability
Budget blown                     → mandatory postmortem, no new features until root cause fixed
```
This is the actual mechanism, not a slogan: it gives engineering a legitimate, pre-agreed reason to say "no more risky changes this month" that isn't a subjective argument with product — the budget already ran out, that's just what happened.

## Choosing the right SLI — harder than it sounds, and easy to get wrong

A bad SLI measures something that doesn't reflect real user pain:

```
Bad:  "server CPU < 80%"            — users don't experience CPU usage
Good: "% of checkout requests completing successfully in < 2s"  — users experience exactly this
```
**Measure from the user's perspective, as close to their actual experience as you can get** — synthetic checks and real user monitoring (RUM) beat internal infrastructure metrics as SLIs, even though infrastructure metrics remain essential for *diagnosing* a problem once an SLI signals one exists.

## Toil — the concept that justifies automation investment with a straight face

**Toil**: manual, repetitive, automatable operational work that scales linearly with service size and provides no lasting value once done (restarting a stuck service by hand, manually rotating a credential, copy-pasting the same investigation steps every incident).

The SRE argument, stated plainly: toil is a tax that grows forever unless you pay it off with automation. A healthy team target is toil under roughly half of an SRE's time — above that, the team is purely reactive and structurally can't get ahead of the problem, because every hour spent on toil is an hour not spent eliminating the next hour of toil.

## The 100% reliability trap

100% reliability is the wrong target for almost everything — chasing it has real, usually invisible costs: massively slower release velocity, an infrastructure bill scaled for a rounding error's worth of failure, engineers afraid to touch anything. **Decide the reliability the product/business actually needs, not the maximum theoretically achievable** — a note-taking app's checkout flow (if it had one) doesn't need five-nines any more than a payment system can tolerate three.

## Blameless culture — not a nicety, a mechanism

If an incident's postmortem can end a career, people will hide information, delay reporting, and route around monitoring that might implicate them — all of which makes the *next* incident worse and slower to diagnose. **Blameless** doesn't mean no accountability; it means the accountability is systemic ("why did our systems/process allow this mistake to cause an outage") rather than personal ("whose fault was it") — because the systemic question is the one that actually prevents a repeat, and the personal one mostly just teaches people to stay quiet next time.

## Where this connects

SLOs need [observability](../devsecops/monitoring/observability.md) to measure against. Error-budget-driven decisions feed directly into [incident management](incident-management.md) and [deployment practices](../devsecops/README.md). None of this works without genuinely good [monitoring/alerting](../devsecops/monitoring/observability.md) as the foundation underneath it.
