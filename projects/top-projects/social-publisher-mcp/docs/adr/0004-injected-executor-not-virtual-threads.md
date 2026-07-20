# 4. Fan out over an injected executor, not hard-coded virtual threads

Status: accepted

## Context

The orchestrator publishes to several platforms at once. The original design note reached for virtual
threads (`spring.threads.virtual.enabled=true`). Virtual threads were finalised in Java 21; this
project targets Java 17, where the API isn't available.

## Decision

`PublicationService` takes an `ExecutorService` in its constructor and fans out with
`CompletableFuture.supplyAsync(..., executor)`. The app provides a small named fixed pool. The core
module stays on Java 17 with no thread-model assumptions baked in.

## Consequences

The fan-out is bounded by a platform-thread pool instead of virtual threads. For this workload —
at most six outbound calls per publication — that's plenty; the calls are I/O-bound and the pool
size comfortably covers the platform count. Because the executor is injected, moving to virtual
threads later is a one-line change in the app wiring once the runtime is on Java 21+, with nothing
to touch in core. The `spring.threads.virtual.enabled` flag is left in config as a no-op on 17 that
Spring will honour automatically on a newer runtime.
