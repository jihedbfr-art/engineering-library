# 04 — Local Inference

Not every workload should hit a hosted API. Data residency, offline development, and
per-token cost at volume are all real reasons to run models on your own hardware via
[Ollama](https://ollama.com) — see [local-inference-tradeoffs.md](local-inference-tradeoffs.md)
for when that trade is actually worth making, plus concrete quantization and hardware sizing
numbers instead of vibes.

[local_ollama_chat.py](local_ollama_chat.py) is the client, plus two ways to trade local
compute time for answer quality — the "test-time scaling" idea, made concrete without
needing a model natively trained for extended reasoning:

- **Self-consistency** — sample the same prompt N times at nonzero temperature, take the
  modal answer. Cheap insurance against a single bad reasoning chain.
- **Iterative refinement** — draft, critique, revise, bounded by a max round count, stopping
  early once the critique step reports no more issues.

Both are visible, inspectable loops rather than a hidden reasoning trace — you can print
every intermediate draft and see exactly where quality improved (or didn't).

`ollama pull llama3` before running this, or point `DEFAULT_MODEL` at whatever you have
pulled locally — DeepSeek and Mistral variants work the same way through the same client.
