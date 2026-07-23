# Model routing and cost control

Every module so far picks one model and calls it. That's fine for a demo and wrong for a
production system serving enough traffic that the per-request model choice actually shows up on
an invoice. This module is about the layer that decides *which* model answers a given request,
and the cache that avoids answering the same question twice at full price.

## Routing: not every request needs the expensive model

A support bot answering "what are your hours" and a support bot drafting a refund-policy
exception for an angry customer are not the same request, even if they hit the same endpoint.
Routing means classifying the request cheaply, then sending it to the model tier that actually
matches what it needs:

- **Simple, high-volume, low-risk** (FAQ lookups, routing/classification, short rewrites) → the
  cheapest model that clears your quality bar on that task specifically, not the best model you
  have access to. This is where most of the token spend usually lives, and where routing pays
  for itself fastest.
- **Complex reasoning, low-volume, or high-stakes** (anything touching money, legal language, a
  multi-step plan) → the strongest model available, because the cost of a wrong answer here
  dwarfs the token cost difference.
- **Everything in between** → this is where teams either build a real classifier or, more often
  and more honestly, start with a handful of rules (request length, presence of certain keywords,
  which endpoint called it) and only add a classifier once the rules visibly stop being good
  enough. Don't build the classifier first; you don't have the failure examples yet to train it
  well.

The router in [`model_router.py`](model_router.py) is rule-based on purpose, for the same reason
[`08-guards-safety`](../08-guards-safety/safety-and-guardrails.md) starts with rules before a
classifier — it's legible, it's free, and it's usually most of the value.

## Semantic caching: the same question asked a different way

Exact-match caching (hash the prompt, look up the answer) catches identical repeats and nothing
else. A semantic cache embeds the incoming request and checks it against previously-answered
requests by similarity — "what's your return window" and "how long do I have to return
something" get treated as the same cached answer instead of two separate paid calls.

This is exactly a vector search problem, which is why the cache in this module is built directly
on [`09-vector-layer`](../09-vector-layer/vector-store-abstraction.md)'s `VectorStore` rather than
its own bespoke index — the interface designed there (embed, upsert, search by similarity) is the
whole mechanism a semantic cache needs; there's no reason to reinvent it.

The part that's easy to get wrong: **a similarity threshold that's too loose returns a cached
answer to a genuinely different question.** "What's your return window" and "what's your
warranty window" are close in embedding space and mean different things. The threshold in this
module's demo is set conservatively (0.8 similarity required, and the demo uses a crude
bag-of-words stand-in for a real embedding, so treat the number itself as illustrative rather
than a value to copy into production) for exactly that reason — a cache miss costs one extra
model call; a wrong cache hit costs a wrong answer that looks confident.

## Cost tracking: know the number before someone asks for it

The cheapest way to avoid an unpleasant surprise on the LLM bill is a running counter, not a
monthly reconciliation. `CostTracker` in this module records estimated cost per call, tagged by
which tier of the router served it, so "how much of our spend actually needed the expensive
model" is a query, not a guess reconstructed from provider logs after the fact.

## What's out of scope here

Actual per-provider pricing tables age fast and belong in configuration, not in a code file that
gets read as a pattern reference — this module takes a price-per-1k-tokens number as an input
rather than hardcoding OpenAI's or Anthropic's current rates. Multi-tenant budget enforcement
(cutting off *tenant X* specifically, not the whole system) is a related but separate concern —
see the TODO in this module's README.
