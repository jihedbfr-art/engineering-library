# Resilience4j — Circuit Breakers That Actually Protect Something

[spring-cloud-patterns.md](spring-cloud-patterns.md) mentions circuit breaking at the gateway level. This is the layer below: wiring Resilience4j directly around a specific call site, which is where the real tuning happens — the gateway-level breaker is a blunt instrument, a service-level one can actually reason about the failure mode of one dependency.

## The state machine, briefly

`CLOSED` (calls pass through, failures counted) → `OPEN` (calls short-circuit immediately, no network call at all) → `HALF_OPEN` (a limited number of calls allowed through to test recovery) → back to `CLOSED` or `OPEN` depending on the result. The point of `OPEN` isn't just to protect the caller — it's to stop hammering an already-struggling dependency, which is often what turns a slow degradation into a full outage.

## Wiring it around a Feign client

```java
@FeignClient(name = "billing-service")
public interface BillingClient {
    @GetMapping("/api/accounts/{id}/balance")
    BalanceResponse getBalance(@PathVariable String id);
}

@Service
class BalanceService {

    private final BillingClient billingClient;
    private final CircuitBreaker circuitBreaker;

    BalanceService(BillingClient billingClient, CircuitBreakerRegistry registry) {
        this.billingClient = billingClient;
        this.circuitBreaker = registry.circuitBreaker("billing-service");
    }

    BalanceResponse getBalance(String accountId) {
        Supplier<BalanceResponse> call = () -> billingClient.getBalance(accountId);
        return circuitBreaker.executeSupplier(
            Retry.decorateSupplier(retryRegistry.retry("billing-service"), call)
        );
    }
}
```

```yaml
resilience4j.circuitbreaker:
  instances:
    billing-service:
      sliding-window-type: COUNT_BASED
      sliding-window-size: 20
      failure-rate-threshold: 50        # % of calls that must fail to trip OPEN
      wait-duration-in-open-state: 15s  # how long before trying HALF_OPEN
      permitted-number-of-calls-in-half-open-state: 5
      slow-call-duration-threshold: 2s  # a call this slow counts as a "failure" too
      slow-call-rate-threshold: 80
```

That last pair — `slow-call-duration-threshold` / `slow-call-rate-threshold` — is the one people skip and then wonder why the breaker never trips during a degradation where the dependency doesn't error out, it just gets slow. A dependency returning 200s at 8 seconds each will happily keep a naive breaker `CLOSED` while it quietly takes the whole call chain down with it.

## Senior trap to avoid

Wrapping the circuit breaker around a call that has no meaningful fallback. If `getBalance()` failing means the whole request has to fail anyway, the breaker is only saving you the network round-trip during an outage — useful, but don't sell it internally as "resilience" if there's nothing degraded to fall back to. The real value of Resilience4j shows up when there's an actual fallback path: cached last-known value, a default, a "try again later" response the caller can act on. Circuit breaker without fallback is just a fancy timeout.

## Circuit breaker vs retry — order matters

Retry *then* circuit breaker (as in the snippet above), not the other way round. If you circuit-break first and retry second, a single call that trips the breaker gets retried against an already-open breaker — pointless, and it burns the retry budget on calls that were never going to reach the network. Decorate order: `Retry.decorateSupplier` wraps the raw call, then `CircuitBreaker.executeSupplier` wraps the retrying supplier — so each retry attempt is what the breaker counts, and once it's `OPEN` further retries short-circuit instantly instead of waiting out their backoff for nothing.

## TODO

Haven't written up bulkheads (`ThreadPoolBulkhead` / `SemaphoreBulkhead`) yet — worth its own entry, the failure mode it protects against (one slow dependency exhausting the shared thread pool and starving unrelated calls) is different enough from what a circuit breaker alone catches.

## Related

- [spring-cloud-patterns.md](spring-cloud-patterns.md) — gateway-level circuit breaking, OpenFeign fallback pattern
- [engineering-failures/kafka-consumer-rebalance-storm.md](../../engineering-failures/kafka-consumer-rebalance-storm.md) — a slow downstream call causing cascading failure, same root shape as an untuned circuit breaker
