# 10 — Model Routing & Cost Control

Not every request needs your strongest, most expensive model. This module is the layer that
decides which tier answers a given request, caches semantically-repeated requests so they're not
paid for twice, and tracks what it actually cost.

[`model-routing-and-cost-control.md`](model-routing-and-cost-control.md) covers the reasoning:
why routing starts as rules (same logic as the guard chain in
[`08-guards-safety`](../08-guards-safety/)), why a semantic cache needs a conservative similarity
threshold or it starts returning wrong answers to different-but-similar-looking questions, and
why pricing tables don't belong hardcoded in this file.

[`model_router.py`](model_router.py) implements a `ModelRouter` (keyword/length rules → cheap or
strong tier), a `SemanticCache` built directly on
[`09-vector-layer`](../09-vector-layer/)'s `VectorStore` — reusing the same embed/search
mechanism rather than a bespoke cache index — and a `CostTracker` that records spend per call,
broken down by tier.

TODO: per-tenant budget caps (cutting off one customer's spend without affecting everyone else)
are a related but separate concern this module doesn't cover yet — it needs a tenant-scoped
identity in the request path first, which doesn't exist anywhere in this library today.
