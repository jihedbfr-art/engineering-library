# Distributed Tracing — Following One Request Across Services

[observability.md](observability.md) covers the three pillars at a high level. This goes one level deeper into traces specifically, because they're the pillar people configure last and need first the day an incident actually spans multiple services.

## What a trace actually is

A **trace** is a tree of **spans**. One span per unit of work (an HTTP call, a DB query, a Kafka publish), each with a start time, duration, and a parent span ID linking it back to whatever triggered it. The `trace_id` is the thread that ties every span across every service back to the single originating request — that's the whole value proposition: one ID, one `grep`, the entire request path across N services instead of N separate log searches hoping the timestamps line up.

```
trace_id: abc123
├── span: gateway → notes-service (12ms)
│   └── span: notes-service → billing-service (340ms)   ← there's your slow call
│       └── span: billing-service → postgres query (335ms)
└── span: gateway → auth-service (4ms)
```

That shape — one deeply nested branch dominating the total — is what a real latency incident looks like in a trace viewer. You almost never need to read every span; you scan for the one span whose duration is close to the total request duration.

## Context propagation — the part that actually breaks

The trace only stays connected if every service forwards the trace context headers (`traceparent`, W3C Trace Context standard) to whatever it calls next. This is automatic for standard HTTP clients once instrumented, but breaks silently the moment a call goes through something the instrumentation doesn't see:

- A message published to Kafka without the trace context in the message headers — the trace ends at the producer and a fresh one starts at the consumer, with no link between them.
- A call made through a raw `HttpClient`/`RestTemplate` that bypasses the instrumented client the framework normally wires up.
- An async task handed off to a thread pool without explicitly propagating the current context — the child thread starts with no trace at all.

The failure mode is quiet: nothing errors, the trace just stops, and six months later someone's staring at two unconnected traces trying to work out if they're related.

## Wiring it in Spring Boot

```xml
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
</dependency>
```
```yaml
management:
  tracing:
    sampling:
      probability: 1.0   # 100% in dev; drop this hard in prod (see sampling below)
otel:
  exporter:
    otlp:
      endpoint: http://tempo:4317
```
Auto-instrumentation covers incoming/outgoing HTTP, JDBC, and (with the Kafka instrumentation module) message headers. Manual span creation for anything auto-instrumentation can't see:
```java
Span span = tracer.spanBuilder("recompute-billing-summary").startSpan();
try (Scope scope = span.makeCurrent()) {
    doExpensiveWork();
} finally {
    span.end();
}
```

## Sampling — don't trace everything in production

100% sampling is fine in dev, and a mistake at real production volume — the storage and network cost of every single span for every single request adds up fast, and most of it is never looked at. Two practical strategies:
- **Head-based sampling**: decide at the start of the trace, probabilistically (e.g. 5-10%). Cheap, simple, but you might miss the exact slow/failed request you actually wanted.
- **Tail-based sampling**: buffer spans briefly and decide *after* seeing the whole trace — keep everything that errored or exceeded a latency threshold, sample the rest lightly. Better signal, more infrastructure (a collector that can hold and evaluate complete traces before deciding).

Start with head-based sampling; only reach for tail-based once trace volume and storage cost force the issue — it's meaningfully more moving parts to operate.

## Related

- [observability.md](observability.md) — metrics and logs, the other two pillars
- [engineering-failures/kafka-consumer-rebalance-storm.md](../../engineering-failures/kafka-consumer-rebalance-storm.md) — exactly the kind of cross-service latency issue a trace would have made obvious in minutes instead of hours of log correlation
