# Java

The enterprise workhorse: strongly typed, JVM-based, huge ecosystem, backward-compatible for decades. Runs banks, Android, and half the backend world.

## Modern Java (17/21) is not your grandpa's Java

```java
// Records — immutable data carriers (no boilerplate)
record User(Long id, String name, boolean active) {}

// var — local type inference
var users = new ArrayList<User>();

// Enhanced switch (expression, no fall-through)
String label = switch (status) {
    case ACTIVE, TRIAL -> "billable";
    case CANCELLED     -> "gone";
    default            -> "unknown";
};

// Text blocks
String json = """
    { "name": "Ada", "active": true }
    """;

// Pattern matching
if (obj instanceof User u && u.active()) {
    System.out.println(u.name());   // u is typed & in scope
}
```

## Streams — functional data processing

```java
List<String> names = users.stream()
    .filter(User::active)
    .map(User::name)
    .sorted()
    .toList();

Map<Boolean, List<User>> byActive =
    users.stream().collect(Collectors.partitioningBy(User::active));

double avg = users.stream().mapToInt(User::age).average().orElse(0);
```

## Collections — pick the right one

| Interface | Common impl | Use |
|---|---|---|
| `List` | `ArrayList` | ordered, indexed, duplicates |
| `Set` | `HashSet` / `TreeSet` | uniqueness / sorted uniqueness |
| `Map` | `HashMap` / `TreeMap` | key→value / sorted keys |
| `Queue`/`Deque` | `ArrayDeque` | FIFO/LIFO |

## Optional — no more null chaos

```java
Optional<User> found = repo.findById(id);
String name = found.map(User::name).orElse("unknown");
found.ifPresent(u -> notify(u));
```
Return `Optional` instead of null from lookups; don't overuse it for fields/parameters.

## The ecosystem you'll actually meet

- **Spring Boot** — the default framework for web/services → [spring guide](../backend/microservices/spring-microservices.md)
- **Maven / Gradle** — build & dependencies
- **JUnit 5 + Mockito + AssertJ** — testing
- **Jackson** — JSON; **Lombok** — kills getter/setter boilerplate

## Gotchas

- **`==` compares references** for objects; use `.equals()` for value equality (and implement `equals`/`hashCode` together — records do it for free).
- **Checked exceptions** must be caught or declared — a Java-specific tax.
- **Autoboxing** `Integer`/`int` can null-pointer or hurt performance in hot loops.
- **Mutable static state** = concurrency bugs. Prefer immutability.
