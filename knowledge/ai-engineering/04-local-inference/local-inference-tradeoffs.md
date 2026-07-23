# Local Inference — Sizing, Quantization, and When to Self-Host

[local_ollama_chat.py](local_ollama_chat.py) shows *how* to run a model locally. This file
covers the decision underneath: whether to, which size to run, and what quantization actually
costs you in exchange for fitting a bigger model on smaller hardware.

## The decision, before the how

Self-hosting a model trades a per-token bill for a fixed hardware/ops cost, and trades API
convenience for operational responsibility. That trade is worth making when:

- **Data residency or compliance requires it** — the input literally cannot leave your
  infrastructure (regulated data, contractual data-locality terms).
- **Volume is high and steady enough** that the amortized hardware cost beats the per-token API
  cost — this is a real calculation, not a vibe: total tokens/month × API price per token, versus
  hardware amortization + power + ops time, over the hardware's realistic useful life.
- **Offline or air-gapped operation is a requirement**, not a preference.
- **Latency to a specific region matters** more than the quality gap between a large hosted model
  and the largest local model your hardware can run at acceptable speed.

It's usually the wrong trade when volume is low/spiky (a hosted API's pay-per-token model already
handles that elasticity better than idle owned hardware) or when the task needs frontier-model
quality that no locally-runnable model size currently matches closely enough.

## Quantization — the lever that makes local inference practical

A model's weights are normally stored as 16-bit floating point. Quantization stores them at lower
precision (8-bit, 4-bit, even lower) to shrink memory footprint and speed up inference, at some
cost to output quality. The practical effect: a model that needs ~14GB of memory at 16-bit can
often run in ~4-5GB at 4-bit quantization — the difference between "needs a serious GPU" and
"runs on a laptop."

| Quantization | Relative size | Typical quality impact |
|---|---|---|
| FP16 (unquantized) | 100% (baseline) | None — reference quality |
| 8-bit (Q8) | ~50% | Negligible for most tasks |
| 4-bit (Q4) | ~25-30% | Small, sometimes noticeable on reasoning-heavy or precise tasks |
| 2-3 bit | ~15-20% | Noticeable degradation, usually a last resort for very constrained hardware |

The right default for most local-inference use: **start at 4-bit** (Ollama's default quantization
for most models pulled without an explicit tag) and only move to 8-bit or full precision if you
measure a real quality gap on your actual task — not preemptively, and always measured against an
eval set (see [05-evaluation-observability](../05-evaluation-observability/)), not by feel.

## Sizing hardware to a model

The rule of thumb that holds up in practice: **memory needed ≈ parameter count × bytes per
parameter**, plus overhead for context and activation memory during inference (roughly 10-20%
more on top for typical usage, more for very long contexts).

```
7B model,  4-bit  → ~4-5GB   → runs on most modern laptops, even without a dedicated GPU
13B model, 4-bit  → ~7-9GB   → comfortable on a mid-range GPU or a capable laptop
70B model, 4-bit  → ~40GB    → needs a serious GPU or multi-GPU setup, not a laptop
```
CPU-only inference works for smaller models (7B and under, quantized) at usable-for-development
speed; anything larger without a GPU becomes painfully slow for interactive use, though still
viable for offline batch processing where latency doesn't matter.

## What quality actually costs you at each tier

Smaller/more-quantized local models close the gap on narrow, well-specified tasks (classification,
extraction into a known schema, code completion within a familiar pattern) faster than they close
it on open-ended reasoning, long-context synthesis, or tasks requiring broad world knowledge. This
is exactly why [self-consistency and iterative refinement](local_ollama_chat.py) matter more for
local models than for a large hosted one — trading extra local compute time for quality is a much
better deal when the compute is free (already-owned hardware) than when it isn't.

## Prevention against the most common local-inference regret

Benchmarking a local model on a handful of manual prompts and declaring it "good enough" without
a real eval set is how teams end up discovering the quality gap in production instead of before
shipping. The eval set built for a hosted-model baseline (see
[05-evaluation-observability](../05-evaluation-observability/)) is the same one to run against a
candidate local model — if it can't pass the same bar, quantization level or model size needs
another look before it goes anywhere near production traffic.

## See also

- [local_ollama_chat.py](local_ollama_chat.py) — the client, plus self-consistency and iterative
  refinement to trade compute time for quality on a smaller model
- [05-evaluation-observability](../05-evaluation-observability/) — the eval set that should gate
  any local-model quality decision, not intuition
