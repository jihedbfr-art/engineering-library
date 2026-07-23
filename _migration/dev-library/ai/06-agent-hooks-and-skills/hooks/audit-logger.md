# Recipe: audit logger (structured, replayable trail)

**Problem:** "what did the agent actually do?" needs to be answerable after the fact, in a format
a script can parse — not scattered across free-text log lines that need to be read by a human to
reconstruct the sequence of actions.

**Hook points:** `PRE_TOOL_USE` and `POST_TOOL_USE`, high priority number (runs after any veto
hooks — an audit entry should record what a security gate denied too, not skip logging on denial).

```python
import json
import time
from agent_hooks import HookContext, HookResult

def audit_log_entry(ctx: HookContext, *, outcome: str, extra: dict | None = None) -> None:
    entry = {
        "timestamp": time.time(),
        "session_id": ctx.session_id,
        "point": ctx.point.value,
        "tool_name": ctx.payload.get("tool_name"),
        "arguments": ctx.payload.get("arguments"),
        "outcome": outcome,
        **(extra or {}),
    }
    # append-only — an audit log that can be edited after the fact isn't an audit log
    with open("audit.jsonl", "a", encoding="utf-8") as f:
        f.write(json.dumps(entry) + "\n")

def audit_pre_tool_use(ctx: HookContext) -> HookResult:
    audit_log_entry(ctx, outcome="attempted")
    return HookResult()

def audit_post_tool_use(ctx: HookContext) -> HookResult:
    audit_log_entry(ctx, outcome="completed", extra={"output_length": len(str(ctx.payload.get("output", "")))})
    return HookResult()
```

**Registration:**
```python
registry.register(HookPoint.PRE_TOOL_USE, audit_pre_tool_use, name="audit_logger", priority=90)
registry.register(HookPoint.POST_TOOL_USE, audit_post_tool_use, name="audit_logger", priority=90)
```

**Why JSONL, not free-text logs:** one JSON object per line makes the audit trail directly
queryable (`jq`, load into a dataframe, feed into `eval_harness.py`-style tooling) without a
parsing step — the same principle as the golden-set format in
[05-evaluation-observability](../../05-evaluation-observability/).

**Failure mode to avoid:** logging only successful calls. A security gate's denial is exactly the
kind of event an audit trail exists to capture — if `audit_post_tool_use` only fires on success,
every blocked attempt (arguably the most security-relevant event) goes unrecorded.
