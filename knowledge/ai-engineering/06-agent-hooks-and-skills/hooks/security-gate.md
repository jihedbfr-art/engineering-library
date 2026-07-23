# Recipe: security gate (pre-tool-use veto)

**Problem:** an agent with shell/file-write tools can be steered — by a bad prompt, a poisoned
tool result, or a plain mistake — into a destructive action. A logging hook after the fact
doesn't prevent anything; only a hook that can deny the call before it runs does.

**Hook point:** `PRE_TOOL_USE`, low priority number (runs before observability hooks, so a
denied call is never logged as if it succeeded).

```python
from agent_hooks import HookContext, HookDecision, HookResult

DENYLIST_PATTERNS = ("rm -rf", "DROP DATABASE", "DROP TABLE", ":(){ :|:& };:")
PROTECTED_PATHS = (".env", "id_rsa", "credentials.json")

def security_gate(ctx: HookContext) -> HookResult:
    if ctx.payload.get("tool_name") == "run_shell":
        command = ctx.payload.get("arguments", {}).get("command", "")
        if any(p in command for p in DENYLIST_PATTERNS):
            return HookResult(decision=HookDecision.DENY, reason="matched a destructive command pattern")

    if ctx.payload.get("tool_name") in ("write_file", "delete_file"):
        path = ctx.payload.get("arguments", {}).get("path", "")
        if any(p in path for p in PROTECTED_PATHS):
            return HookResult(decision=HookDecision.DENY, reason=f"write/delete blocked on protected path: {path}")

    return HookResult()  # allow — this hook only denies, it never approves on behalf of others
```

**Registration:**
```python
registry.register(HookPoint.PRE_TOOL_USE, security_gate, name="security_gate", priority=10)
```

**Why a denylist here, not an allowlist:** for a general-purpose coding/shell tool, an allowlist
of "safe" commands is impractical to keep complete — a denylist of known-catastrophic patterns is
the pragmatic middle ground. For a narrower, high-stakes tool (a payments API, an infra-provisioning
tool), invert this: allowlist the specific calls that are safe, deny everything else by default.

**Failure mode to avoid:** a security gate that only checks `tool_name` and never inspects
`arguments` catches nothing — the same tool can be safe or catastrophic depending entirely on its
arguments (`write_file` to a scratch path vs. `write_file` to `.env`).
