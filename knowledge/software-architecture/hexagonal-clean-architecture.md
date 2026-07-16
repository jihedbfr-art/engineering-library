# Hexagonal / Clean Architecture

Different names (Hexagonal, Ports & Adapters, Clean Architecture, Onion Architecture), the same core idea, arrived at independently by several people because the underlying problem is real and recurring: **business logic keeps getting silently entangled with frameworks, databases, and delivery mechanisms, until you can't change or test one without dragging in all the others.**

## The dependency rule — the one idea that generates everything else

```
                    ┌─────────────────────────────┐
                    │   Frameworks, DB, HTTP,       │   ← outer layer:
                    │   external APIs (infrastructure)│     depends INWARD only
                    └───────────────┬─────────────┘
                    ┌───────────────▼─────────────┐
                    │      Application layer          │   ← orchestrates use cases,
                    │      (use cases, ports)          │     depends INWARD only
                    └───────────────┬─────────────┘
                    ┌───────────────▼─────────────┐
                    │        Domain layer              │   ← pure business logic,
                    │   (entities, business rules)     │     depends on NOTHING outer
                    └─────────────────────────────┘
```
**Dependencies only ever point inward, never outward.** The domain layer — your actual business rules — doesn't import Spring, doesn't import your database driver, doesn't know HTTP exists. The outer layers depend on the domain; the domain never depends on them. This single, strictly-enforced rule is what makes everything else in this pattern actually work.

## Ports and adapters — the mechanism that enforces the rule

```java
// PORT — an interface defined BY the domain/application layer,
// describing what it needs, in the domain's own vocabulary
public interface NotificationSender {
    void send(Subscriber subscriber, String message);
}

// ADAPTER — the concrete implementation, living in the OUTER, infrastructure layer
@Component
public class TwilioNotificationSender implements NotificationSender {
    public void send(Subscriber subscriber, String message) {
        twilioClient.sendSms(subscriber.getPhoneNumber(), message);   // Twilio specifics stay HERE
    }
}
```
The **port** is an interface the domain defines, in its own terms — the domain says "I need to notify a subscriber somehow," without caring how. The **adapter** is the concrete, framework/vendor-specific implementation, living entirely in the outer layer. Swap Twilio for AWS SNS, or for a completely different notification channel: implement a new adapter against the exact same port; **the domain layer changes not one single line.** This is the direct, practical payoff of the dependency rule — not an abstract architectural nicety, a genuine business capability (swap vendors without touching business logic).

## Why this is worth the extra indirection — the concrete payoff

```java
// Domain layer — pure Java, ZERO framework imports, no @Component, no @Autowired
public class SubscriptionService {
    private final NotificationSender sender;    // depends on the PORT (an interface), not Twilio

    public void notifyExpiringSubscription(Subscriber s) {
        if (s.daysUntilExpiry() <= 3) {
            sender.send(s, "Your subscription expires soon");    // pure business logic
        }
    }
}
```
1. **Testability without a framework or a real database.** Unit-test `SubscriptionService` with a trivial fake `NotificationSender` — no Spring context to boot, no database to spin up, no HTTP mocking. Fast, focused tests that verify actual business logic, not framework wiring.
2. **The domain survives framework churn.** Migrate from Spring to a different framework, or from REST to gRPC, and the actual business rules — the part that took real domain knowledge to get right — are completely untouched, because they never depended on any of that in the first place.
3. **New team members can read the domain layer and understand the actual business rules**, without wading through `@Transactional`, dependency injection wiring, and ORM annotations cluttering what should be a clear statement of business logic.

## A concrete "before and after," to make the payoff tangible

```java
// BEFORE — business logic tangled directly with Spring Data JPA and infrastructure concerns
@Service
public class OrderService {
    @Autowired private OrderRepository repo;         // JPA repository — a framework/infra dependency
    @Transactional                                    // a Spring/infra concern, embedded directly here
    public void submitOrder(Long orderId) {
        Order order = repo.findById(orderId).orElseThrow();
        order.setStatus("SUBMITTED");                 // business rule, entangled with infra plumbing
        repo.save(order);
    }
}

// AFTER — domain logic pure, infrastructure concerns pushed to the outer adapter layer
public class Order {                                   // domain entity — zero framework imports at all
    public void submit() {
        if (this.status != OrderStatus.DRAFT) throw new IllegalStateException(...);
        this.status = OrderStatus.SUBMITTED;
    }
}
public class SubmitOrderUseCase {                       // application layer — orchestrates, stays framework-light
    private final OrderRepositoryPort repo;              // a PORT — an interface, not Spring Data directly
    public void execute(OrderId id) {
        Order order = repo.findById(id);
        order.submit();                                   // the actual business rule lives HERE now
        repo.save(order);
    }
}
// The concrete Spring Data JPA implementation of OrderRepositoryPort lives entirely
// in the outer, infrastructure layer — the domain and application layers never see it
```

## The honest cost — this is a real tradeoff, not a free upgrade

More files, more interfaces, more indirection for genuinely simple operations — a straightforward CRUD screen with no real business logic doesn't need three layers and a port/adapter pair; that's meaningfully more ceremony for zero corresponding payoff. **The value of this pattern scales directly with how much real, non-trivial business logic actually exists** to protect from framework entanglement — a thin CRUD wrapper around a database table has essentially nothing worth protecting this way, and applying it there is a recognizable case of pattern-for-its-own-sake rather than genuine engineering judgment.

## Where this connects

This is the natural tactical execution layer for the [bounded contexts and aggregates](domain-driven-design.md) DDD identifies — DDD tells you *what* the domain model should look like; hexagonal architecture is *how* you keep that model honestly isolated from infrastructure concerns once you've built it. [Spring Cloud patterns](../backend/java-spring/spring-cloud-patterns.md) elsewhere in this library assume this kind of clean separation exists as the foundation resilience/timeout/circuit-breaker logic gets layered onto, at the outer adapter boundary, not scattered through domain code.
