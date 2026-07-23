# Design Patterns — Reusable Solutions

Named solutions to problems that recur in software. Use them as **vocabulary** ("let's put a factory here") — not as goals in themselves. The best code uses the fewest patterns that solve the problem.

## Creational — how objects get made

**Factory** — a method decides which concrete class to instantiate.
```python
def make_parser(kind):
    return {"json": JsonParser, "xml": XmlParser}[kind]()
```
Use when creation logic is complex or the concrete type varies at runtime.

**Builder** — construct a complex object step by step.
```java
User u = User.builder().name("Ada").email("ada@x.com").admin(true).build();
```
Use when a constructor would have 6+ parameters or many optional ones.

**Singleton** — one shared instance. Use *sparingly* — it's global state in disguise; prefer dependency injection. Legit for config, connection pools, loggers.

## Structural — how objects compose

**Adapter** — wrap an incompatible interface to match what you need. (The classic "translate library A's API into our expected interface".)

**Decorator** — add behavior by wrapping, without changing the original.
```python
@retry(times=3)
@cache
def fetch(url): ...
```

**Facade** — a simple interface over a complex subsystem. Your service layer over a tangle of clients is a facade.

**Proxy** — a stand-in that controls access (lazy loading, caching, access control, remote calls).

## Behavioral — how objects interact

**Strategy** — swap an algorithm at runtime via a common interface.
```python
sort(items, key=strategy)      # different comparison strategies
payment.process(gateway)       # stripe vs paypal, same interface
```
Kills big `if/elif` chains over a "type".

**Observer** — subjects notify subscribers of changes. Event systems, pub/sub, UI reactivity, Kafka consumers.

**Command** — wrap an action as an object → undo/redo, queues, logging of operations.

**Template method** — a base class defines the skeleton; subclasses fill in steps.

**State** — object behavior changes with internal state, without giant conditionals.

## The most valuable meta-lessons

1. **Program to an interface, not an implementation** — depend on abstractions so parts are swappable and testable.
2. **Favor composition over inheritance** — deep inheritance trees are rigid; small composed objects are flexible.
3. **Don't force patterns.** A pattern used where a plain function would do is *added* complexity. Reach for one when you feel the specific pain it solves.

> Anti-pattern warning: if your "clean architecture" has 8 files to print "hello", you've over-patterned. Simplicity is the real goal.
