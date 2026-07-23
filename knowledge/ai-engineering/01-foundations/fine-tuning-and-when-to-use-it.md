# Fine-Tuning — and When It's the Wrong Answer

[llm-fundamentals.md](llm-fundamentals.md) puts fine-tuning last in the decision order
("prompting before RAG, RAG before fine-tuning — complexity is a cost") without saying why. This
file covers the why, what fine-tuning actually changes, and the cases where it's genuinely the
right tool instead of the expensive one people reach for too early.

## What fine-tuning actually does

Prompting and RAG both work at inference time — they change what the model *sees*, not what it
*is*. Fine-tuning changes the model's weights through additional training on a task-specific
dataset. That distinction has a concrete consequence: a fine-tuned model can't be un-taught a
behavior by editing a prompt, and it can't be handed new factual knowledge by editing a system
message the way RAG can — the knowledge or behavior has to already be baked into the weights.

## The three things fine-tuning is actually good for

- **Consistent style/format at volume**, cheaper per call than a long few-shot prompt repeated on
  every request — if you're paying token cost for 10 examples of the exact output format on every
  single call, a fine-tuned model that's internalized the format pays that cost once, at training
  time, instead of on every request forever.
- **A narrow task the base model does adequately but not reliably enough** — classification into
  your specific taxonomy, extraction into your specific schema, a house style that few-shot
  prompting approximates but doesn't nail consistently.
- **Latency/cost at extreme volume** — a smaller fine-tuned model matching a larger general
  model's quality on one narrow task, if the volume justifies the training and evaluation cost.

## What fine-tuning is a bad answer for

- **"The model doesn't know about our product/docs/policies."** This is a knowledge-injection
  problem, not a behavior problem — [RAG](../02-rag-architectures/) solves it, is far cheaper to
  keep current, and doesn't require retraining every time the underlying facts change. A
  fine-tuned model with stale knowledge baked into its weights is *harder* to fix than a RAG
  index with a stale document — you retrain instead of just re-indexing.
- **"The model's answers aren't accurate enough."** Almost always a retrieval or prompting problem
  (see [rag-concepts.md](../02-rag-architectures/rag-concepts.md) on debugging retrieval before
  generation) — fine-tuning on a small dataset can just as easily teach the model to be
  confidently wrong in a new, harder-to-detect way, because it now sounds fluent while still
  lacking the actual source of truth.
- **"We want the model to do X reliably" without a golden eval set already in hand.** Fine-tuning
  without an eval set to measure against is flying blind twice over — no way to tell if the base
  model's prompted behavior was actually the ceiling of what prompting alone could do before
  paying the fine-tuning cost. Build the eval set first (see
  [05-evaluation-observability](../05-evaluation-observability/)); it's needed either way, and it
  might show that a better prompt closes the gap without any training run at all.

## The decision, concretely

```
Missing knowledge, changes over time         → RAG, not fine-tuning
Format/style inconsistent under heavy volume → fine-tuning is a real candidate
Behavior wrong on edge cases                 → check the eval set: is it a prompting gap
                                                 or a genuine capability gap? Prompting gaps
                                                 (unclear instructions, missing examples) are
                                                 far more common than capability gaps.
Task narrow, volume very high, latency matters → smaller fine-tuned model may beat a bigger
                                                   general model on cost/latency for that
                                                   one task specifically
```

## The two flavors, briefly

- **Full fine-tuning** — every weight updates. Expensive, needs real infrastructure, mostly a
  choice for teams training their own base models, rare for application-layer engineering.
- **Parameter-efficient fine-tuning (LoRA and variants)** — a small number of additional
  low-rank weight matrices are trained while the base model stays frozen, merged or applied at
  inference time. Dramatically cheaper, the practical default for most task-specific fine-tuning
  today, and the reason fine-tuning became accessible outside of teams with large training
  budgets.

## Prevention against the most common regret

The single most avoidable mistake: fine-tuning on a snapshot of data, shipping it, and having no
way to retrain cheaply when the task drifts (new categories appear, house style changes, edge
cases the training set didn't cover start showing up in production). Treat the training dataset
with the same version-control and provenance discipline as code — know exactly what went in, so
the next iteration is a re-run, not an archaeology project.

## See also

- [llm-fundamentals.md](llm-fundamentals.md) — the decision table this file expands on
- [ml-fundamentals.md](ml-fundamentals.md) — train/validation/test discipline applies identically
  to a fine-tuning dataset, it's still supervised learning underneath
- [05-evaluation-observability](../05-evaluation-observability/) — build the eval set before,
  not after, deciding fine-tuning is necessary
