# System Design — Scaling from 1 to Millions

How to think about building systems that stay up and fast as they grow. This is the interview topic *and* the real job.

## The journey of a growing system

```
1 server (app + db)                          → simple, single point of failure
→ split app & database                       → scale independently
→ add a load balancer + multiple app servers → horizontal scale, redundancy
→ add caching (Redis) + CDN                  → take load off the DB, serve static fast
→ read replicas / sharding on the DB         → scale reads, then writes
→ async work via queues                      → smooth spikes, decouple
→ split into services where teams/scale need it
```
Each step solves the bottleneck the previous one created. Don't jump to the end — complexity is a cost.

## Core building blocks

| Block | Solves |
|---|---|
| **Load balancer** | Spread traffic, health-check, failover |
| **Horizontal scaling** | Add machines (stateless apps) vs bigger machine (vertical, has a ceiling) |
| **Cache** (Redis/Memcached) | Repeated reads — the biggest, cheapest win |
| **CDN** | Static assets served near the user |
| **Message queue** (Kafka/SQS/RabbitMQ) | Decouple, absorb spikes, async processing |
| **Database replica** | Scale reads |
| **Sharding / partitioning** | Scale writes / data too big for one node |

## Caching — the highest ROI lever

- Cache what's read often and changes rarely.
- **The hard part is invalidation**: TTL (expire after N seconds) is simplest; explicit invalidation on write is precise but tricky.
- Patterns: cache-aside (app checks cache, then DB, then fills cache) is the common default.
- Watch for: thundering herd (many misses at once), stale data, cache stampede.

## SQL vs NoSQL (right tool, not fashion)

- **SQL**: relationships, transactions, strong consistency, ad-hoc queries. Default choice — most apps are relational.
- **NoSQL**: massive scale, flexible schema, specific access patterns (document, key-value, wide-column, graph). Choose for a *reason*, not hype.
- You often use **both**: Postgres for core data, Redis for cache/sessions, a search engine for full-text.

## CAP theorem in one breath

Under a network partition, you choose **Consistency** (reject/stall to stay correct) or **Availability** (answer, maybe stale). You can't have both during a partition. Most systems tune between them per operation.

## Scalability principles

1. **Stateless services scale horizontally** — push state to DB/cache/session store, then add machines freely behind a load balancer.
2. **Async the slow stuff** — email, image processing, reports → queue + workers. Return fast to the user.
3. **Cache aggressively, invalidate carefully.**
4. **Avoid single points of failure** — replicate anything whose death stops the system.
5. **Design for failure** — timeouts, retries with backoff, circuit breakers, graceful degradation.
6. **Measure**: you can't scale what you don't observe → [observability](../devsecops/monitoring/observability.md).

## A framework for design questions

1. **Clarify**: functional needs, scale (users, QPS, data size), read/write ratio, latency targets.
2. **Estimate**: back-of-envelope QPS, storage, bandwidth.
3. **High-level design**: draw the boxes (client → LB → services → data stores → queue).
4. **Deep-dive** the tricky part (the data model, the hot path, the bottleneck).
5. **Address bottlenecks & failures**: caching, sharding, replication, what breaks and how you recover.

## Trade-offs, always

There is no "best" architecture — only the right trade-off for your constraints. More consistency costs availability/latency. More services cost operational complexity. Name the trade-off you're making; that's what separates senior design from cargo-culting.
