# 🚨 Site Reliability Engineering (SRE)

DevSecOps asks "how do we ship safely." SRE asks "how do we keep it running, and how do we decide when 'good enough' actually is good enough." Google coined the term; the core idea has since become standard practice everywhere running production systems at scale.

- [sre-fundamentals.md](sre-fundamentals.md) — SLIs/SLOs/error budgets, toil, the SRE mindset
- [incident-management.md](incident-management.md) — on-call, severity levels, postmortems that actually improve things
- [capacity-planning.md](capacity-planning.md) — forecasting load, load testing, scaling decisions

## How this differs from DevSecOps

They overlap heavily and many teams do both under one roof. The distinction that's actually useful: [DevSecOps](../devsecops/README.md) is about the **path to production** (pipelines, security gates, deployment). SRE is about **what happens once it's live** — measuring reliability quantitatively, deciding how much risk to spend on new features vs. stability, and running a disciplined incident response when it breaks anyway. [Observability](../devsecops/monitoring/observability.md) is the shared foundation both disciplines build on.
