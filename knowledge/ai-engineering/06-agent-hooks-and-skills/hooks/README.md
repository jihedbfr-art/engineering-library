# Hooks catalog

Concrete recipes built on the `HookRegistry` in [`../agent_hooks.py`](../agent_hooks.py) — each
one is a real hook function plus the reasoning for its priority and hook point, not just a
snippet. Read [`../hooks-pattern.md`](../hooks-pattern.md) first for the underlying model
(lifecycle points, veto vs. observe vs. rewrite, fail-closed on error) — these recipes assume it.

| Recipe | Hook point | Mechanism | Purpose |
|---|---|---|---|
| [`security-gate.md`](security-gate.md) | `PRE_TOOL_USE` | veto (deny) | Block destructive shell commands and writes to protected paths before they execute |
| [`audit-logger.md`](audit-logger.md) | `PRE_TOOL_USE` + `POST_TOOL_USE` | observe | Append-only, queryable JSONL trail of every tool call attempted and completed, including denials |
| [`cost-tracker.md`](cost-tracker.md) | `PRE_MODEL_CALL` | veto (deny) | Hard-stop a session once its real accumulated token cost crosses a budget |
| [`pii-redaction.md`](pii-redaction.md) | `POST_TOOL_USE` | rewrite | Strip emails/phone numbers/keys/card numbers from a tool result before it re-enters context or gets logged |
| [`rate-limiter.md`](rate-limiter.md) | `PRE_TOOL_USE` | veto (deny) | Cap call frequency per tool in a rolling window, catching a runaway loop before the session cost budget even notices |
| [`context-window-guard.md`](context-window-guard.md) | `PRE_MODEL_CALL` | rewrite | Trim message history to stay under a token budget without ever dropping the original task framing |
| [`human-approval-gate.md`](human-approval-gate.md) | `PRE_TOOL_USE` | veto (deny until approved) | Structurally pause irreversible actions (send, delete, deploy, pay) on explicit human confirmation, not just a prompt instruction |

## Composing more than one

These aren't mutually exclusive — a real deployment typically registers several on the same hook
point, ordered by priority so the sequence is deliberate, not accidental:

```python
registry = HookRegistry()

# Security first: nothing else should see a call that gets denied outright
registry.register(HookPoint.PRE_TOOL_USE, security_gate, priority=10)
# Cost gate before the model call that would spend the budget
registry.register(HookPoint.PRE_MODEL_CALL, tracker.pre_model_call_gate, priority=10)
# Redact before anything downstream logs or re-uses the raw output
registry.register(HookPoint.POST_TOOL_USE, redact_pii, priority=5)
# Audit last on each point, so it records the already-redacted, already-gated reality
registry.register(HookPoint.PRE_TOOL_USE, audit_pre_tool_use, priority=90)
registry.register(HookPoint.POST_TOOL_USE, audit_post_tool_use, priority=90)
```

The ordering isn't arbitrary: security and cost gates run first because a denied call shouldn't
be logged as if it happened, and redaction runs before audit logging so the stored trail never
contains the raw PII in the first place — redact once, upstream, rather than trusting every
downstream consumer to redact independently.
