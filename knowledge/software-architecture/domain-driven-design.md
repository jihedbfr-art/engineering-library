# Domain-Driven Design (DDD)

A methodology for tackling genuinely complex business domains — not "how do I structure a CRUD app" (that's usually overkill for DDD), but "our business logic is inherently intricate, and the code needs to actually reflect that complexity honestly instead of hiding it behind generic CRUD abstractions that don't fit."

## The core insight, before any of the tactical patterns

Most software project failures aren't caused by bad code — they're caused by a **model mismatch**: the software's internal model of the business doesn't actually match how the business really works, so every new requirement fights the existing code instead of extending it naturally. DDD's actual thesis: invest deliberately in building a model that genuinely reflects the real domain, in close collaboration with the people who actually understand that domain — everything else in DDD (bounded contexts, aggregates, ubiquitous language) is tooling in service of that one core goal.

## Ubiquitous language — the deceptively simple idea that fixes a real, chronic problem

```
Without it:  Business says "customer" — means someone with an active contract.
             Dev team's code says "User" — actually means anyone who ever registered,
             active contract or not.
             Every conversation between business and dev has a silent, unnoticed
             mismatch neither side realizes is even happening.

With it:     Business and code use the EXACT SAME TERM, meaning the EXACT SAME THING.
             If the business says "Subscriber," the class is called Subscriber,
             not Account, not User, not Customer — and its meaning matches
             precisely what a business stakeholder means when they say the word.
```
This sounds almost too simple to matter — until you've sat through a meeting where "cancel" meant something different to three different people in the room, and the code silently encoded only one of those three meanings, with nobody noticing until it caused a real production bug months later. The ubiquitous language isn't a naming convention exercise; it's a genuine communication tool that surfaces domain misunderstandings *before* they become bugs, precisely because a wrong shared term gets challenged out loud in conversation long before it gets challenged by a failing test.

## Bounded contexts — the answer to "one model can't fit the whole business"

```
   E-commerce platform, several genuinely different meanings of "Product":

   ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
   │  Catalog Context      │  │  Inventory Context    │  │  Shipping Context      │
   │  Product = marketing   │  │  Product = SKU, stock  │  │  Product = weight,      │
   │  description, images,  │  │  level, warehouse       │  │  dimensions, fragility  │
   │  price, category        │  │  location                │  │  class                  │
   └─────────────────────┘  └─────────────────────┘  └─────────────────────┘
```
Trying to force one single unified `Product` class to correctly serve all three contexts is a recognizable, common source of bloated, constantly-changing "god classes" that every team eventually regrets. **A bounded context is an explicit boundary where a specific model applies, consistently, and stops applying past that boundary.** Different contexts can use the same word to mean genuinely different things — and that's fine, deliberately, as long as the boundary itself is explicit rather than accidental. This maps directly onto [microservice boundaries](../backend/microservices/spring-microservices.md) in practice — a well-chosen bounded context is very often exactly the right size and shape for a single microservice, which is why DDD and microservices architecture are so often discussed together.

## Aggregates — the consistency boundary, defined deliberately

```java
public class Order {                    // the Aggregate Root — the ONLY entry point
    private OrderId id;
    private List<OrderLine> lines;       // internal to the aggregate — never touched directly from outside
    private OrderStatus status;

    public void addLine(Product product, int quantity) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify a submitted order");
        }
        lines.add(new OrderLine(product, quantity));
    }
    // External code NEVER does order.getLines().add(...) directly —
    // it always goes through addLine(), which enforces the actual business invariant
}
```
An **aggregate** groups entities that must stay consistent *together*, with a single **aggregate root** as the only allowed entry point for any change — external code is never permitted to reach into an aggregate's internals and mutate them directly, bypassing the root's business rules. This is what actually prevents a whole class of real, insidious bugs: an `OrderLine` added to a `Draft` order behaves completely differently, safely, from one silently added to an already-`Submitted` order via some forgotten, unguarded direct-access code path elsewhere in the codebase.

**Picking the right aggregate boundary is genuinely one of the harder judgment calls in DDD** — too large (e.g. "the whole Order plus every related Shipment and Invoice" as one aggregate) and you get real, painful contention/locking issues under concurrent access, echoing exactly the [transaction/concurrency problems](../databases/transactions-concurrency.md) covered elsewhere. Too small and you lose the actual consistency guarantee the pattern exists to provide in the first place. There's no universal formula — it's a genuine domain-knowledge judgment call, made in collaboration with people who understand the actual business invariants that matter.

## Strategic vs tactical DDD — the distinction worth keeping straight

```
Strategic DDD:  the BIG decisions — bounded contexts, how contexts relate
                to each other (a "context map"), which contexts matter
                most to the business ("core domain") vs which are
                necessary-but-generic supporting infrastructure.

Tactical DDD:   the CODE-LEVEL patterns — entities, value objects,
                aggregates, repositories, domain events. The specific
                implementation patterns most tutorials focus on almost
                exclusively.
```
A very common, real mistake: teams adopt tactical DDD's code patterns (entities, aggregates, repositories) without ever doing the strategic work of actually identifying bounded contexts and the real core domain — producing code that *looks* like DDD superficially, with all the right class names, but doesn't capture the actual strategic value the methodology was created to deliver in the first place.

## When DDD is genuinely worth the investment, and when it isn't

✅ Reach for it when: the business domain is genuinely, inherently complex (insurance underwriting rules, logistics routing, [telecom provisioning workflows](../../telecom/provisioning-architecture.md), financial trading rules) — domains where the complexity is real and won't go away no matter how the code is organized.

⚠️ Skip the ceremony for: a straightforward CRUD app, an internal admin tool, a genuinely simple domain — DDD's overhead (ubiquitous language workshops, careful aggregate design, strategic context mapping) is a real, deliberate cost that only pays for itself against genuine domain complexity. Applying full DDD ceremony to a simple todo-list app is a textbook example of solving a problem you don't actually have.

## Where this connects

Bounded contexts map naturally onto [microservice boundaries](../backend/microservices/spring-microservices.md) — DDD is frequently the actual strategic thinking underneath a well-designed microservices decomposition, rather than services being split by guesswork or organizational convenience alone. [Hexagonal architecture](hexagonal-clean-architecture.md) is the natural tactical partner to DDD — it's specifically about keeping a carefully-modeled domain (the whole point of everything above) from getting silently entangled with framework and infrastructure code.
