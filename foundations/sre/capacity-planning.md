# Capacity Planning

Deciding how much infrastructure you need, before you need it — the discipline that sits between "we have no idea if this will handle Black Friday" and "we're paying for 10x the capacity we ever use."

## The core question, stated precisely

Not "will this handle load" — but **"will this handle load at the 95th/99th percentile of realistic traffic, with the failure margin we're comfortable with, at the cost we're willing to pay."** Every clause in that sentence is a real decision, not a detail.

## Load testing — finding the ceiling before your users do

```
Baseline test:   normal expected traffic, confirm current behavior/latency
Stress test:     ramp load until something breaks — find the actual ceiling
Soak test:       moderate load sustained for HOURS — catches memory leaks,
                  connection pool exhaustion, disk fill-up that a 5-minute
                  test never reveals
Spike test:      sudden traffic surge (a flash sale, a viral post) —
                  does autoscaling react fast enough, or does the surge
                  land before new capacity is even up?
```
Gatling, k6, Locust — the tool matters far less than actually running these against a production-representative environment. A load test against a staging environment with 1/10th the data and no realistic network topology tells you very little about production behavior — representativeness is what makes the test worth running at all.

## Reading a load test result properly

```
Throughput: 500 req/s
p50 latency: 45ms      ← the median user's experience
p95 latency: 180ms     ← 1 in 20 requests is this slow or worse
p99 latency: 2400ms    ← 1 in 100 — often where the real problem hides
Error rate: 0.02%
```
**Never plan capacity around the average.** A p50 of 45ms with a p99 of 2400ms means real users are having a genuinely bad time on a regular, non-rare basis — the average alone completely hides that. Capacity/performance decisions belong at p95/p99, not p50, precisely because those are the requests real users actually complain about.

## Forecasting demand — combine methods, don't rely on just one

- **Historical trend**: past growth rate, seasonality (a retailer's December, a tax system's filing deadline — this is a very literal, direct parallel to [government ERP systems](legacy-modernization.md) hitting deadline-driven traffic spikes).
- **Business signals**: an upcoming marketing campaign, a new market launch, a scheduled deadline that's not visible in historical data at all because it hasn't happened yet.
- **Synthetic worst-case modeling**: "what if every current customer logged in within the same hour" — deliberately unrealistic, useful as a genuine upper bound, not a forecast.

## Horizontal vs vertical scaling — the tradeoff, honestly stated

| | Vertical (bigger machine) | Horizontal (more machines) |
|---|---|---|
| Complexity | Low — no architecture change needed | Higher — needs statelessness, load balancing |
| Ceiling | Hard limit (biggest instance available) | Much higher, if the architecture supports it |
| Cost curve | Non-linear — big instances cost disproportionately more | More linear, plus better fault tolerance as a side benefit |
| Downtime to scale | Often requires a restart | Usually zero-downtime (add nodes) |

Most systems should be **designed to scale horizontally from day one** (stateless services, externalized session state) even if they run on one instance today — retrofitting statelessness onto a system that assumed a single instance from the start is a much larger, riskier project than building it in from the beginning.

## Autoscaling — the part everyone assumes "just works"

```yaml
# Kubernetes HPA example
minReplicas: 3
maxReplicas: 20
targetCPUUtilizationPercentage: 70
```
The trap: autoscaling reacts to metrics **after** load has already started climbing — if a new pod takes 90 seconds to boot and the load spike arrives in 20 seconds, autoscaling is structurally too slow for that specific spike, no matter how well-tuned the thresholds are. For predictable spikes (a scheduled batch job, a known campaign launch, a known regulatory filing deadline), **scheduled pre-scaling** ahead of the known event beats reactive autoscaling every time — plan for the spike you can see coming instead of trusting the autoscaler to react fast enough to it.

## Database capacity — usually the actual bottleneck, not the app tier

Stateless app services scale out easily; your database usually can't, not nearly as easily. Real levers, roughly in order of how often they're actually the right first move: **connection pooling tuned correctly** (a shockingly common root cause of "the app can't handle load" that has nothing to do with the app), **read replicas** for read-heavy workloads, **caching** in front of hot queries ([Redis](../databases/nosql.md)), and only then genuine **sharding** — sharding is the most powerful lever and also the most operationally expensive, so it's usually the last resort, not the first idea.

## Where this connects

Capacity planning is what turns an [SLO](sre-fundamentals.md) from a number on a slide into infrastructure that can actually hit it. It leans directly on [observability](../devsecops/monitoring/observability.md) data to know current real-world load, and on [database indexing/transactions](../databases/indexing-deep-dive.md) knowledge to find where the actual database ceiling sits before a real spike finds it for you.
