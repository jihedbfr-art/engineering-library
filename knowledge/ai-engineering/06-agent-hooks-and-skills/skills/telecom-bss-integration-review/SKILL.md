---
name: telecom-bss-integration-review
description: Review integration code between a BSS/OSS platform and a telecom core network or provisioning system — subscriber activation, number portability, SIM provisioning connectors. Use when reviewing code that talks to a telecom provisioning platform, a portability gateway, or any multi-step subscriber lifecycle flow, or when asked to check "provisioning code", "connector reliability", or "portability flow" for correctness.
---

# Telecom BSS/provisioning integration review

Provisioning and portability flows share a specific risk profile that generic API-integration
review misses: they're multi-step, cross-system, and touch a subscriber's actual service — a
half-applied step doesn't just fail cleanly, it can leave a real customer without service or
double-billed. Review for these failure modes specifically.

## What to check, in priority order

1. **Idempotency of every provisioning call.** A connector retry (network blip, timeout) must
   not re-trigger a side effect that isn't safe to repeat — activating a SIM twice, re-submitting
   a portability request, re-charging a one-time fee. Every write to the core network or
   provisioning platform needs either a natural idempotency key (order/request ID passed through
   the whole chain) or an explicit check-before-write. Flag any retry logic wrapped around a call
   with no idempotency guarantee.

2. **Partial-failure handling in multi-step flows.** Subscriber activation and number portability
   are sequences (validate → reserve → activate → confirm, or similar). If step 3 of 4 fails,
   what happens to steps 1-2? Flag any flow with no explicit compensation/rollback path for a
   mid-sequence failure — "we'll just retry from the start" is wrong if steps 1-2 aren't safe to
   redo (see idempotency above).

3. **Correlation ID propagation.** A provisioning request can touch three or more systems
   (BSS, provisioning platform, core network element, billing). If a single correlation/order ID
   doesn't propagate through every hop and every log line, a production incident becomes an
   archaeology exercise across disconnected logs instead of one traceable chain. Flag any
   connector call that doesn't pass the correlation ID through, or any log statement missing it.

4. **Timeout and retry policy matched to the real SLA.** A provisioning platform call that can
   legitimately take 30+ seconds (a core network round-trip) needs a timeout and retry policy
   that reflects that reality — a 3-second timeout with 5 rapid retries against a naturally slow
   system multiplies load on an already-struggling dependency instead of backing off. Check that
   retry backoff is present and that the timeout was chosen from a measured SLA, not copied from
   an unrelated fast internal service.

5. **Asynchronous response handling.** Many provisioning platforms respond asynchronously
   (submit now, callback/poll later). Flag any code that treats the synchronous submission
   acknowledgment as if it were the final result — "submitted successfully" and "provisioned
   successfully" are different states, conflating them is a recurring real-world bug class in
   this domain.

## How to report findings

For each issue, name the specific step in the flow, the concrete scenario that triggers it (a
timeout mid-sequence, a duplicate retry, a callback that never arrives), and whether it risks a
customer-visible outcome (double activation, stuck request, silent data loss) versus just a
logging/observability gap — customer-visible risks are the ones to lead with.

## What NOT to flag

- Synchronous calls to genuinely fast, idempotent-by-nature lookups (subscriber status queries)
  don't need the same rigor as state-changing provisioning calls — reserve the checklist above
  for anything that changes subscriber state.
- Don't flag missing distributed tracing infrastructure as a blocker if correlation IDs are
  already propagated manually through logs — that's a lower-priority observability upgrade, not
  a correctness bug.
