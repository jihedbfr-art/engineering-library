---
name: observability-instrumentation-reviewer
description: Review a service's logging, metrics, and tracing for real gaps — spans that don't propagate context, metrics with no alerting attached, log lines that can't be correlated to a request. Use when asked to "review our observability", "check if this is properly instrumented", or before a service goes to production without anyone having deliberately looked at what it emits.
---

# Observability instrumentation reviewer

Most services aren't under-instrumented in volume — they log plenty. They're under-instrumented in
the specific way that matters at 3am: nothing ties a log line to the request that caused it, a
metric exists but no alert reads it, or a trace stops at the service boundary instead of following
the request into the next hop. This skill looks for those specific gaps, not "more logging."

## What to check, in order of how often it actually bites someone

1. **Correlation ID propagation.** Does every log line inside a request's lifecycle carry the same
   request/trace ID, and does that ID survive across service boundaries (HTTP headers, message
   queue metadata)? If a support ticket says "this failed around 2pm," can every log line for that
   one request actually be found, or does the trail go cold at the first downstream call?
2. **Metrics with no alert, and alerts with no metric.** A dashboard nobody watches until after an
   incident isn't observability, it's decoration. For each metric collected, is there a
   threshold/anomaly rule attached, or does someone have to remember to look? Inversely, for each
   alert configured, is it backed by a metric that actually reflects user impact (latency, error
   rate) rather than an internal implementation detail nobody outside the team understands?
3. **Log level discipline.** Is `ERROR` reserved for things that need a human, or does it also
   fire on expected, handled conditions (a 404, a validation failure)? Noisy `ERROR` logs are how
   real errors get lost in the scroll — this is the single most common finding in a first-pass
   review.
4. **Trace spans stop where the interesting work starts.** A span wrapping the whole HTTP handler
   but nothing around the database call or the external API call inside it tells you a request was
   slow, not why. Check whether spans exist around every external call (DB, cache, third-party API,
   queue publish) specifically, not just at the handler boundary.
5. **Cardinality bombs.** A metric labeled by `user_id` or `request_id` isn't a metric anymore,
   it's an unbounded label explosion that will eventually take down or bankrupt the metrics
   backend. Flag any label with unbounded or high-cardinality values.
6. **Silent failure paths.** A `catch` block, a fallback default, a retry-then-give-up — anywhere
   an error is handled instead of propagated is a place observability has to compensate, because
   the caller will never know something went wrong. Check that every one of these emits *something*
   (a log line at minimum, a counter increment ideally) rather than swallowing silently.

## What NOT to flag

- Don't recommend adding a metric or a log line without saying what decision it would inform —
  "add more logging" without a specific question it answers is the review equivalent of the
  problem it's trying to fix.
- Don't treat every `DEBUG`-level log as noise to remove; the question is whether `ERROR`/`WARN`
  are disciplined, not whether verbose levels exist at all.

## Output shape

A short list, ranked by how often the gap actually causes a slow incident response versus a
theoretical nice-to-have — a missing correlation ID across a service boundary usually outranks a
missing metric on an internal cache hit ratio, even though both are real gaps.
