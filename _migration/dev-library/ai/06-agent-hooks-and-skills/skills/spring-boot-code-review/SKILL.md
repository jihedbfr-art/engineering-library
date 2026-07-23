---
name: spring-boot-code-review
description: Review a Spring Boot / JPA diff for the specific bug classes that pass tests and CI but still break in production — transaction self-invocation, silent rollback gaps, N+1 queries, connection/thread leaks, and missing bounds on external calls. Use when reviewing a Java/Spring pull request, before merging a service-layer change, or when asked to "review this Spring Boot code" or "check this PR for issues".
---

# Spring Boot code review — the bugs that don't show up in tests

Generic code review catches style. This skill catches the bug classes that are structurally
invisible to a green test suite and a passing build, because they only manifest under real
concurrency, real transaction boundaries, or a slow/failing dependency — the conditions unit
tests rarely reproduce.

## What to check, in priority order

1. **`@Transactional` self-invocation.** Flag any call from one method to another
   `@Transactional` method on the *same class* via `this.method(...)` — Spring's proxy is
   bypassed, so the target method runs with no transaction at all, silently. Only a call through
   the injected bean (a different class, or self-injection) goes through the proxy.

2. **Rollback on checked exceptions.** Spring only rolls back automatically on
   `RuntimeException`/`Error`. If a `@Transactional` method throws a checked exception without
   `rollbackFor` explicitly set, partial writes can commit despite the exception. Flag any
   business exception that extends `Exception` directly instead of `RuntimeException`, and any
   `@Transactional` without `rollbackFor` where the method's throws clause includes a checked type.

3. **N+1 queries.** Look for a loop over a JPA collection where each iteration triggers a lazy
   load (`order.getItems().forEach(item -> item.getProduct().getName())` without a fetch join or
   `@EntityGraph` on the original query). One query became N+1 — invisible on a test database
   with 3 rows, real in production with 3,000.

4. **Unbounded external calls.** Any `RestTemplate`, `WebClient`, or JDBC call with no explicit
   connect/read timeout configured. Under normal conditions this never triggers; the first time
   the downstream dependency degrades, every thread that hits this code path blocks — see
   thread-pool exhaustion below. A missing timeout is always worth flagging, even if the call
   "has always been fast."

5. **Resource leak potential.** Any `Connection`, `EntityManager`, or stream opened without
   try-with-resources or a guaranteed `finally`. If an exception can be thrown between open and
   close, the resource leaks on that path — trace every exit path of the method, not just the
   happy path.

6. **Thread-pool exposure.** A slow synchronous call on a request-handling thread (Tomcat's
   pool) with no circuit breaker and no bulkhead isolation risks starving the whole application
   from one degraded dependency, not just the endpoint that calls it. Flag this especially on
   endpoints that call an external/third-party service directly.

## How to report findings

For each issue found, report: file:line, which bug class from the list above, the concrete
failure scenario (not "this could be a problem" — describe the actual input/timing that triggers
it), and the minimal fix. Skip anything that's purely stylistic — this skill is about
correctness bugs invisible to CI, not formatting.

## What NOT to flag

- Missing `@Transactional` on read-only query methods — not every method needs one.
- Lazy loading itself — only lazy loading *inside a loop* without batching is the N+1 problem.
- Long methods, naming, or other style concerns — out of scope for this skill.
