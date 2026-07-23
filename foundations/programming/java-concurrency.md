# Java Concurrency — Deep Dive

[java.md](java.md) covers the language overview. This is the part that actually separates a senior Java engineer from someone who's used `@Async` a few times without fully knowing why it sometimes bites — genuinely relevant if you're running Spring Boot services under real production load, not just a demo.

## The Java Memory Model — why concurrent code can be wrong in ways that "look right"

```java
// Two threads, one shared flag. Looks obviously correct. Isn't guaranteed to be.
class Flag {
    boolean ready = false;    // NOT volatile
    int value = 0;
}
// Thread A: flag.value = 42; flag.ready = true;
// Thread B: while (!flag.ready) {}  System.out.println(flag.value);
```
Without `volatile` (or proper synchronization), the JVM and the CPU are both explicitly permitted to **reorder** these writes, and Thread B may never observe `ready` becoming `true` at all — not a rare edge case, a real, specified consequence of the Java Memory Model's relaxed guarantees, which exist specifically to let the JIT compiler and CPU optimize aggressively. The fix:

```java
class Flag {
    volatile boolean ready = false;   // establishes a happens-before edge
    int value = 0;
}
```
`volatile` guarantees that a write to `ready` in Thread A **happens-before** a subsequent read of `ready` in Thread B — and critically, everything Thread A wrote *before* setting `ready` (including `value = 42`) becomes visible to Thread B too, once B observes `ready == true`. This "happens-before" relationship is the actual formal contract the entire Java Memory Model is built on — understanding it properly is what separates "this concurrent code happened to work in testing" from "this concurrent code is actually, provably correct."

## Threads are expensive — the constraint that shaped Java concurrency for 20 years

```java
// Each platform thread costs real, non-trivial resources:
// - ~1MB of stack memory by default, reserved per thread
// - real OS-level scheduling overhead
Thread t = new Thread(() -> doWork());
t.start();
// Spawning 100,000 of these will exhaust memory or crash the JVM outright —
// this is a hard, real ceiling, not a soft performance degradation
```
This single constraint — a platform thread is genuinely expensive — is why Java concurrency built an entire ecosystem of patterns specifically to *avoid* creating threads carelessly: thread pools, executors, and eventually a completely new lightweight-threading model (virtual threads, below). Every pattern in this page traces back, one way or another, to working around this one constraint.

## ExecutorService — the standard way to actually manage threads

```java
ExecutorService executor = Executors.newFixedThreadPool(10);   // a bounded pool of 10 reusable threads

Future<Integer> future = executor.submit(() -> {
    return computeSomething();
});
Integer result = future.get(5, TimeUnit.SECONDS);   // blocks, with a timeout — NEVER omit the timeout

executor.shutdown();   // always shut down explicitly — a leaked executor leaks threads forever
```
A **bounded** thread pool is a deliberate choice, not a limitation to work around — `Executors.newFixedThreadPool(10)` guarantees you'll never have more than 10 concurrent threads doing this work, which is exactly what protects a service from being overwhelmed under a sudden load spike ([capacity-planning](../sre/capacity-planning.md) territory). `Executors.newCachedThreadPool()` (unbounded) is a genuinely common, real production incident waiting to happen under unexpected load — an unbounded pool has no ceiling on how many threads it will spawn, and no ceiling on how many is exactly the kind of thing that turns a load spike into an outage.

## CompletableFuture — composing async work without callback soup

```java
CompletableFuture<User> userFuture = fetchUserAsync(id);
CompletableFuture<List<Order>> ordersFuture = userFuture
    .thenCompose(user -> fetchOrdersAsync(user.getId()));   // chain, don't nest

CompletableFuture<String> combined = userFuture
    .thenCombine(ordersFuture, (user, orders) ->
        user.getName() + " has " + orders.size() + " orders");

combined.exceptionally(ex -> {
    log.error("Failed to combine", ex);
    return "unknown";
}).thenAccept(System.out::println);
```
`thenCompose` (flatten a nested future — use when your callback itself returns a future) vs `thenApply` (transform a plain value — use when your callback returns a plain value) is the single most common real point of confusion here, and it's exactly the same distinction as `flatMap` vs `map` on a stream — once you've internalized one, you've genuinely internalized both. `CompletableFuture` composition is the direct, code-level foundation underneath [reactive/resilient service-to-service calls](../backend/java-spring/spring-cloud-patterns.md) elsewhere in this library.

## Virtual threads (Project Loom, Java 21+) — the actual paradigm shift

```java
// Before: platform threads, genuinely expensive, careful pooling required
ExecutorService executor = Executors.newFixedThreadPool(200);

// After: virtual threads — genuinely, dramatically cheap, spawn millions if you want
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> {
            String result = callBlockingService();   // blocking calls are now CHEAP to make concurrently
            process(result);
        });
    }
}   // each submitted task gets its OWN virtual thread — no pool sizing math required at all
```
Virtual threads are managed entirely by the JVM, not the OS — they're genuinely cheap enough to spawn one per request, or even one per logical unit of blocking work, without any of the traditional thread-pool-sizing anxiety this whole page has built up to. **This is arguably the single biggest practical shift in Java concurrency in over a decade**: it makes the traditionally-discouraged "just block, it's simpler to read" style of code genuinely viable at real production scale again, because a blocked virtual thread costs almost nothing while it waits — a sharp, direct contrast to a blocked platform thread, which sits there holding onto real, non-trivial OS resources for the entire duration of the block.

## The trap virtual threads don't fix — `synchronized` still pins the carrier thread

```java
// This still causes a real performance problem, even on virtual threads:
synchronized (lock) {
    callBlockingService();   // pins the underlying OS carrier thread for the duration —
                              // defeats a real chunk of virtual threads' whole point
}

// Prefer java.util.concurrent locks instead, when the code runs on virtual threads:
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    callBlockingService();   // does NOT pin the carrier thread
} finally {
    lock.unlock();
}
```
This is a genuinely easy trap for experienced Java developers migrating existing code to virtual threads specifically *because* `synchronized` has been idiomatic, safe, unremarkable Java for 25+ years — old habits don't automatically know about this new, subtle interaction, and it's a real, current gotcha worth knowing by name if you're adopting virtual threads in an existing large codebase.

## `synchronized`, locks, and the concurrency utilities toolbox

```java
private final AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();          // lock-free, hardware-CAS-based — genuinely faster under contention

private final ConcurrentHashMap<String, User> cache = new ConcurrentHashMap<>();
cache.computeIfAbsent(id, this::loadUser);   // atomic check-then-act, no manual external locking needed

private final CountDownLatch latch = new CountDownLatch(3);
// three worker threads each call latch.countDown() when finished;
// a coordinating thread calls latch.await() to block until all three are done
```
`java.util.concurrent` exists precisely because hand-rolled `synchronized` blocks around shared mutable state are genuinely easy to get subtly wrong (deadlocks, missed edge cases, forgotten locks around one access path out of several) — the same underlying concurrency discipline as [database transaction/locking correctness](../databases/transactions-concurrency.md), just at the in-process, single-JVM level instead of across a database.

## Where this connects

This is the language-level foundation underneath every [Spring Cloud resilience pattern](../backend/java-spring/spring-cloud-patterns.md) elsewhere in this library — timeouts, circuit breakers, and async service calls are all, underneath, built on exactly the primitives covered on this page. The happens-before/visibility discipline mirrors, at the single-process level, the same fundamental correctness concerns [database transaction isolation](../databases/transactions-concurrency.md) addresses at the distributed/multi-connection level — recognize the pattern once, and it shows up in both places for the same underlying reason.
