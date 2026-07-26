# Recipe: duplicate-call deduper (idempotency key on side-effecting calls)

**Problem:** an agent that calls a side-effecting tool (charge a card, send an email, create a
ticket), gets back a timeout or an ambiguous error, and then retries — reasonably, from its own
point of view — can end up having actually performed the action twice. This is a different failure
from the burst-loop the [rate-limiter](rate-limiter.md) catches: the calls aren't rapid-fire, they
might be minutes apart, and each individual call looks completely legitimate in isolation. The only
thing distinguishing a legitimate retry-after-failure from a duplicate side effect is whether the
first attempt actually succeeded, which the agent often can't tell from a timeout alone.

**Hook point:** `PRE_TOOL_USE`, high priority (before the call executes, obviously, and before
[audit-logger](audit-logger.md) so a deduped call is logged as deduped rather than as a normal
attempt).

```python
import hashlib
import json
import time
from agent_hooks import HookContext, HookDecision, HookResult

class DuplicateCallDeduper:
    def __init__(self, side_effecting_tools: set[str], window_seconds: float = 300):
        self.side_effecting_tools = side_effecting_tools
        self.window_seconds = window_seconds
        self._seen: dict[str, float] = {}  # call fingerprint -> first-seen timestamp

    def _fingerprint(self, tool_name: str, arguments: dict) -> str:
        # Sorted keys so argument order never changes the fingerprint. This is a
        # content fingerprint, not a caller-supplied idempotency key — it catches
        # an agent re-issuing the exact same call, not a legitimately different
        # call that happens to touch the same resource.
        canonical = json.dumps(arguments, sort_keys=True)
        return hashlib.sha256(f"{tool_name}:{canonical}".encode()).hexdigest()

    def check(self, ctx: HookContext) -> HookResult:
        tool_name = ctx.payload.get("tool_name", "")
        if tool_name not in self.side_effecting_tools:
            return HookResult()  # only gate the tools actually capable of a real duplicate side effect

        fingerprint = self._fingerprint(tool_name, ctx.payload.get("arguments", {}))
        now = time.monotonic()
        last_seen = self._seen.get(fingerprint)

        if last_seen is not None and now - last_seen < self.window_seconds:
            return HookResult(
                decision=HookDecision.DENY,
                reason=(
                    f"identical call to '{tool_name}' with the same arguments was already made "
                    f"{now - last_seen:.0f}s ago — likely a retry of a call that already succeeded, "
                    "not a genuinely new request"
                ),
            )

        self._seen[fingerprint] = now
        return HookResult()
```

**Registration:**
```python
deduper = DuplicateCallDeduper(
    side_effecting_tools={"charge_card", "send_email", "create_ticket"},
    window_seconds=300,
)
registry.register(HookPoint.PRE_TOOL_USE, deduper.check, name="duplicate_call_deduper", priority=15)
```

**Why a window, not permanent memory:** a genuinely new request with identical arguments (charging
the same amount to the same card again, a week later) is legitimate and shouldn't be blocked
forever — the window bounds the dedup to the timeframe where "this is probably the same logical
request retried" is actually a safe assumption, not an indefinite one.

**Failure mode to avoid:** listing every tool as side-effecting "to be safe." A read-only tool
gains nothing from this hook and every entry widens the fingerprint cache for no benefit — scope
`side_effecting_tools` to the specific tools where a duplicate call causes real harm (money moved,
a message sent, a resource created twice), the same scoping discipline as
[human-approval-gate](human-approval-gate.md)'s irreversible-action list.

**This doesn't replace a real idempotency key at the API level** — a well-designed downstream API
accepting a client-generated idempotency key is the more robust fix, because it catches duplicates
even across process restarts or multiple agent instances, which this in-memory hook can't. This
hook is the mitigation for tools that don't have that API-level support yet, not a reason to skip
building it.
