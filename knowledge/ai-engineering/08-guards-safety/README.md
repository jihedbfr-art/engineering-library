# 08 — Guards & Safety

The layer that assumes the model will say the wrong thing eventually — because at scale, it will.
[`safety-and-guardrails.md`](safety-and-guardrails.md) covers the input/output guard pattern:
gating text going into the model separately from text coming out of it, why rules alone catch
less than you'd hope, and the fail-open vs. fail-closed decision most teams never actually make on
purpose.

[`input_output_guards.py`](input_output_guards.py) is a small rule-tier guard chain — PII and
prompt-injection checks on the way in, system-prompt-leak and empty-response checks on the way
out — built so a classifier or an LLM-as-judge slots into the same `GuardRule` interface later
without changing the chain itself.

[`red-teaming-methodology.md`](red-teaming-methodology.md) and
[`red_team_probes.py`](red_team_probes.py) are the other half: a fixed suite of known attack
patterns run against the chain above, so a fixed gap can't quietly come back. Running it against
this module's own default chain already found two real gaps — worth reading before assuming rule
lists are more solid than they are.

This sits next to, not instead of, the other control layers already in this library:

| Layer | Question it answers | Module |
|---|---|---|
| Guardrails (here) | Should this text pass through, in either direction? | `08-guards-safety` |
| Hooks | Should this action execute right now? | [`06-agent-hooks-and-skills`](../06-agent-hooks-and-skills/hooks-pattern.md) |
| Plugin permissions | What can this specific tool touch? | [`07-extensibility`](../07-extensibility/plugin-architecture.md) |
| Evaluation | Is the output actually good, independent of safety? | [`05-evaluation-observability`](../05-evaluation-observability/evals-and-testing.md) |
