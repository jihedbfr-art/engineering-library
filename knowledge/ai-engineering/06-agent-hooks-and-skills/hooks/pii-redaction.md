# Recipe: PII redaction (post-tool-use payload rewrite)

**Problem:** a tool result (a database query result, a file read, an API response) can contain
personal data — emails, phone numbers, national ID numbers, API keys — that shouldn't propagate
into the model's context (extra exposure surface, and it'll likely get echoed back in the
response) or into logs/audit trails at all.

**Hook point:** `POST_TOOL_USE`, low priority number (redact before the audit logger or anything
else downstream sees the raw payload) — this is the `modified_payload` mechanism from
[`agent_hooks.py`](../agent_hooks.py), not just an allow/deny decision.

```python
import re
from agent_hooks import HookContext, HookResult

PATTERNS = {
    "email": re.compile(r"[\w.+-]+@[\w-]+\.[\w.-]+"),
    "phone": re.compile(r"\b\+?\d{1,3}[\s.-]?\(?\d{2,4}\)?[\s.-]?\d{3,4}[\s.-]?\d{3,4}\b"),
    "api_key": re.compile(r"\b(sk-|ghp_|AKIA)[A-Za-z0-9]{10,}\b"),
    "credit_card": re.compile(r"\b(?:\d[ -]*?){13,16}\b"),
}

def redact_pii(ctx: HookContext) -> HookResult:
    output = str(ctx.payload.get("output", ""))
    redacted = output
    found_types = []
    for label, pattern in PATTERNS.items():
        if pattern.search(redacted):
            found_types.append(label)
            redacted = pattern.sub(f"[REDACTED_{label.upper()}]", redacted)

    if not found_types:
        return HookResult()

    return HookResult(modified_payload={**ctx.payload, "output": redacted, "_redacted_types": found_types})
```

**Registration:**
```python
registry.register(HookPoint.POST_TOOL_USE, redact_pii, name="pii_redaction", priority=5)
```

**Why regex here instead of an LLM call to detect PII:** a regex pass is fast, deterministic, and
runs on every single tool result without adding a model call (and its own cost/latency) to every
step of the loop — reserve an LLM-based PII detector for cases regex genuinely can't catch (PII
embedded in free-form prose without a recognizable pattern), as a second pass on flagged content,
not the default path for every tool result.

**Failure mode to avoid:** redacting only the model-facing payload while an unredacted copy still
gets written to the [audit log](audit-logger.md) or another downstream sink — redaction has to
happen before the payload branches to every consumer, or specifically apply the same redaction at
each sink independently. A hook registry that runs hooks in a fixed priority order (redaction
before logging) is what makes "redact once, upstream of everything" actually hold.
