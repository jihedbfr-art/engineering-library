# 05 — Evaluation & Observability

Not in the original four-module plan, and added on purpose: every one of the other modules
in this library — RAG, agents, local inference — produces probabilistic output, and
probabilistic output you don't measure is a guess wearing a demo's clothes.

[evals-and-testing.md](evals-and-testing.md) covers building a golden eval set, scoring
methods from free regex matching up to LLM-as-judge, the RAG-specific metrics (retrieval hit
rate before answer quality, always), and wiring evals into CI so a prompt or model change
can't silently regress quality.

[eval_harness.py](eval_harness.py) is that habit made runnable: the full scoring ladder from
free regex matching up to LLM-as-judge, a pass-rate CI gate exactly like the YAML snippet in
evals-and-testing.md (`sys.exit(1)` when the threshold isn't met), and a stand-in system under
test so it runs with zero API keys — swap in a real call (`advanced_rag.py`'s pipeline, a
fine-tuned model, anything that returns text) to grade your own system instead of the demo.

If you only take one habit from this entire `/ai` module, take this one: before you call
anything "improved," show the eval numbers that prove it.
