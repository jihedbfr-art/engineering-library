# Guardrails — the layer that assumes the model will say the wrong thing

Every other module in this library assumes the model does roughly what you asked. This one
doesn't. It assumes the input might be an injection attempt, the output might leak something it
shouldn't, and the tool call in between might be exactly the kind of thing a hook in
[`06-agent-hooks-and-skills`](../06-agent-hooks-and-skills/hooks-pattern.md) is supposed to catch.
Guardrails are the input/output half of that story — hooks gate *actions*, guardrails gate
*text*, and a real system needs both.

## Two different jobs, often confused as one

**Input guarding** answers: should this request even reach the model? Prompt injection buried in
a document the model is about to retrieve, a user trying to extract the system prompt, PII that
shouldn't be forwarded to a third-party API at all — these get caught (or at least flagged)
before a single token of generation happens.

**Output guarding** answers: should this response reach the user? A model that's been
successfully jailbroken, a hallucinated citation, a response that leaks a fragment of the system
prompt it was told to keep private — these only show up after generation, so they need a
separate check on the way out.

Conflating the two is how you end up with a single "safety score" that can't tell you whether the
problem is the request or the response, which matters a lot when you're deciding whether to block
the user or just regenerate.

## Rules vs. classifiers vs. a second model call

Three tiers, roughly increasing in cost and decreasing in false negatives:

| Approach | Catches | Misses | Cost |
|---|---|---|---|
| Regex / keyword rules | Known bad patterns, PII shapes (SSN, card numbers) | Anything paraphrased | Near zero |
| A small classifier | Statistical patterns in injection/toxicity | Novel attacks the classifier wasn't trained on | Cheap, one forward pass |
| LLM-as-judge (a second model call) | Semantic intent, novel phrasing | Cost, latency, and the judge itself can be fooled | Same order of magnitude as the original call |

Production systems layer these rather than picking one — rules first because they're free and
catch the obvious cases, then a classifier or judge for what gets through. Rules alone is what a
lot of teams ship first and then get burned by; I've seen "block the word ignore" catch exactly
zero real jailbreak attempts because nobody types `ignore previous instructions` verbatim anymore.

## Fail-open vs fail-closed, and why it's not obvious

If the guardrail service itself is down — timeout, 500, whatever — does the request go through
ungated, or does it get blocked? There's no universally correct answer here, and teams that pick
one without discussing it usually didn't actually decide, they just got whatever the client
library's default retry/timeout behavior happened to do.

- **Fail-closed** (block on guardrail failure) is the safe default for anything touching PII,
  finance, or an action with side effects — same logic as
  [`hooks-pattern.md`](../06-agent-hooks-and-skills/hooks-pattern.md)'s fail-closed rule for a
  pre-tool-use hook that throws.
- **Fail-open** is defensible for a low-stakes internal tool where availability matters more than
  the marginal risk of one ungated request during an outage — but that has to be a decision
  someone signs off on, not a default nobody looked at.

## What this module ships

[`input_output_guards.py`](input_output_guards.py) is a small, dependency-light guard pipeline:
a chain of input rules (PII pattern match, injection-phrase heuristics) and a chain of output
rules (confidence threshold, banned-phrase leak check), both fail-closed by default and both
overridable per rule. It's rule-tier only — no classifier, no LLM judge — on purpose, so the
pattern is legible without needing an API key to run the demo. Swapping in a classifier or a
judge call is a matter of adding another `GuardRule` to the chain; the chain doesn't care what's
inside a rule.

## Where this sits relative to everything else

| Layer | Question it answers | Module |
|---|---|---|
| Guardrails (this module) | Should this text pass through, in either direction? | `08-guards-safety` |
| Hooks | Should this *action* be allowed to execute right now? | [`06-agent-hooks-and-skills`](../06-agent-hooks-and-skills/hooks-pattern.md) |
| Plugin permissions | What is this specific tool allowed to touch? | [`07-extensibility`](../07-extensibility/plugin-architecture.md) |
| Evaluation | Did the output meet quality bar, independent of safety? | [`05-evaluation-observability`](../05-evaluation-observability/evals-and-testing.md) |

None of these four replace each other. A response can pass every guardrail here and still fail
an eval for being wrong, and a tool call can be perfectly safe by hook standards while the text
around it fails an output guard for leaking something. Treat them as independent gates, not a
single pass/fail.

TODO: this module doesn't yet cover red-teaming methodology (deliberately trying to break your
own guards before someone else does) — that's a big enough topic it probably deserves its own
file rather than a section tacked onto this one. Left out for now rather than done badly.
