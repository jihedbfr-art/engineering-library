# Spring Cloud Patterns — Senior-Level Notes

[backend/microservices](../microservices/spring-microservices.md) covers the fundamentals (Eureka, Keycloak resource server, resilience basics). This is the next layer: the patterns that matter once you're running 10+ services in production, not 2 in a demo.

## Spring Cloud Gateway — the front door done right

```java
@Bean
public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("notes-service", r -> r.path("/api/notes/**")
            .filters(f -> f
                .circuitBreaker(c -> c.setName("notesCB").setFallbackUri("forward:/fallback/notes"))
                .retry(retryConfig -> retryConfig.setRetries(2).setMethods(HttpMethod.GET))
                .requestRateLimiter(rl -> rl.setRateLimiter(redisRateLimiter())))
            .uri("lb://notes-service"))     // lb:// = load-balanced via service discovery
        .build();
}
```
The gateway is where cross-cutting concerns belong so individual services don't reimplement them: rate limiting, circuit breaking, request logging with correlation IDs, JWT validation at the edge (defense in depth still means every service validates too — see [OWASP A01](../../devsecops/security/owasp-top10.md)).

**Senior trap to avoid**: putting business logic in gateway filters. The gateway routes and enforces cross-cutting policy; it should never know what a "note" is.

## OpenFeign — declarative service-to-service calls, done defensively

```java
@FeignClient(name = "billing-service", fallback = BillingFallback.class,
             configuration = FeignConfig.class)
public interface BillingClient {
    @GetMapping("/api/accounts/{id}/balance")
    BalanceResponse getBalance(@PathVariable String id);
}

@Component
class BillingFallback implements BillingClient {
    public BalanceResponse getBalance(String id) {
        return BalanceResponse.degraded();     // never let one dependency crash the caller
    }
}
```
```java
@Configuration
class FeignConfig {
    @Bean
    Request.Options options() {
        return new Request.Options(2000, 5000);   // connect timeout, read timeout — NEVER default/infinite
    }
    @Bean
    Retryer retryer() {
        return new Retryer.Default(100, 1000, 3);  // backoff, not infinite hammering
    }
}
```
The Feign client itself is trivial. What separates a senior implementation from a demo is the **timeout, retry, and fallback configuration around it** — an unconfigured Feign client with default (often unbounded) timeouts is a production outage waiting for its first slow dependency.

## Distributed configuration — Spring Cloud Config

```yaml
# config-server: centralizes config across services, environment-specific,
# refreshable without redeploy (paired with @RefreshScope or Spring Cloud Bus)
spring:
  cloud:
    config:
      uri: http://config-server:8888
      fail-fast: true       # don't start with silently-missing config
```
At scale, hardcoded `application.yml` per service becomes an operational liability — nobody can tell what's actually configured where without checking N repos. Externalized config is what makes "change a rate limit in prod without a redeploy" a 30-second operation instead of a release.

## Saga pattern — distributed transactions without distributed transactions

You can't `@Transactional` across microservices — no shared database, no two-phase commit that scales sanely. The **saga pattern** replaces one ACID transaction with a sequence of local transactions, each with a **compensating action** if a later step fails.

```
1. Order service: create order (local tx)
2. Payment service: charge card (local tx)
   → fails? → Order service: compensate (cancel order)
3. Inventory service: reserve stock (local tx)
   → fails? → Payment service: compensate (refund)
             → Order service: compensate (cancel order)
```
Two flavors:
- **Choreography**: each service listens for events and reacts (via [Kafka](../../data-engineering/streaming-kafka.md)) — no central coordinator, but the overall flow is harder to see in one place.
- **Orchestration**: a central process (often a [BPMN workflow](bpmn-workflow-engines.md)) explicitly drives each step and its compensation — easier to reason about and monitor, at the cost of a coordinating component.

For anything with more than 3-4 steps or real compensation logic, **orchestration with a workflow engine beats choreography** — a saga scattered across five services' event handlers is genuinely hard to debug at 2am; a BPMN diagram of the same saga is not.

## Correlation IDs — the thing that makes distributed debugging possible

```java
// Gateway or first entry point generates it if absent
String correlationId = request.getHeader("X-Correlation-Id");
if (correlationId == null) correlationId = UUID.randomUUID().toString();
MDC.put("correlationId", correlationId);   // now every log line in this thread includes it

// Propagate on every outbound call (Feign interceptor)
@Bean
RequestInterceptor correlationIdInterceptor() {
    return template -> template.header("X-Correlation-Id", MDC.get("correlationId"));
}
```
Without this, debugging "why did this user's request fail" across 6 services means grepping 6 sets of logs by timestamp and hoping. With it, one correlation ID finds every log line across every service for that one request — see [observability](../../devsecops/monitoring/observability.md) for the tracing layer this feeds into.

## The senior-level checklist for a new inter-service call

- [ ] Explicit connect + read timeouts (never framework defaults)
- [ ] Retry policy — and only on idempotent operations
- [ ] Circuit breaker with a real fallback (degraded response, not a 500)
- [ ] Correlation ID propagated
- [ ] What happens to the caller if this dependency is down for 10 minutes? (answer that *before* shipping, not during the incident)
