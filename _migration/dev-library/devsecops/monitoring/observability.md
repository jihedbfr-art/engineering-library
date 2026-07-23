# Observability — Logs, Metrics, Traces

## The three pillars

| Pillar | Question it answers | Tools |
|---|---|---|
| **Metrics** | *Is something wrong?* (numbers over time) | Prometheus + Grafana |
| **Logs** | *What exactly happened?* (events) | Loki / ELK |
| **Traces** | *Where in the chain did it happen?* (request path across services) | Tempo / Jaeger + OpenTelemetry |

In microservices, traces are not optional: one user request = 6 services. Without a `trace_id`, debugging is archaeology.

## The four golden signals (monitor these first)

1. **Latency** — p50/p95/p99, not averages (averages hide pain)
2. **Traffic** — requests/sec
3. **Errors** — rate of 5xx / failed operations
4. **Saturation** — how full: CPU, memory, disk, connection pools

## Prometheus in 60 seconds

Apps expose `/metrics` (Spring Boot: add `micrometer-registry-prometheus`, endpoint appears at `/actuator/prometheus`). Prometheus scrapes them; you query with PromQL:

```promql
# Error rate over 5 minutes
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
/ sum(rate(http_server_requests_seconds_count[5m]))

# p95 latency
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))
```

## Structured logging — do this

```json
{"ts":"2026-07-15T10:12:03Z","level":"ERROR","service":"notes-api",
 "trace_id":"abc123","user_id":"42","event":"note.save.failed","error":"timeout"}
```
- JSON, one event per line → machines can search it.
- Always include `trace_id` and service name.
- **Never log**: passwords, tokens, full card numbers, raw PII.

## Alerting that people don't mute

- Alert on **symptoms** (user-facing error rate, latency SLO burn), not causes (CPU 80%).
- Every alert must be: actionable, urgent, and have a runbook link.
- If an alert fires and the on-call does nothing → delete or fix the alert.

## SLO mini-glossary

- **SLI**: measured indicator (e.g. % of requests < 300ms)
- **SLO**: target (99.9% this month)
- **Error budget**: 100% − SLO — spend it on releases; when exhausted, freeze features and fix reliability.
