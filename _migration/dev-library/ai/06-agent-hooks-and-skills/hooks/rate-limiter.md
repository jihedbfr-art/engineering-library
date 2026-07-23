# Recipe: rate limiter (per-tool call-frequency cap)

**Problem:** a cost or step budget bounds *total* spend over a session, but says nothing about
*burst rate* — an agent stuck in a bad pattern can hammer one tool (a search API, a write
endpoint) dozens of times in rapid succession before the session-level budget even notices,
potentially tripping the tool's own external rate limits or amplifying a mistake before anyone
can intervene.

**Hook point:** `PRE_TOOL_USE`, low-to-mid priority (after a hard security gate, before general
observability — a rate-limited call should still be logged as rate-limited, not silently
dropped).

```python
import time
from collections import defaultdict, deque
from agent_hooks import HookContext, HookDecision, HookResult

class RateLimiter:
    def __init__(self, max_calls: int, window_seconds: float):
        self.max_calls = max_calls
        self.window_seconds = window_seconds
        self._call_times: dict[str, deque] = defaultdict(deque)

    def check(self, ctx: HookContext) -> HookResult:
        tool_name = ctx.payload.get("tool_name", "")
        now = time.monotonic()
        window = self._call_times[tool_name]

        while window and now - window[0] > self.window_seconds:
            window.popleft()

        if len(window) >= self.max_calls:
            return HookResult(
                decision=HookDecision.DENY,
                reason=f"rate limit hit: {self.max_calls} calls to '{tool_name}' per {self.window_seconds}s",
            )

        window.append(now)
        return HookResult()
```

**Registration:**
```python
limiter = RateLimiter(max_calls=5, window_seconds=10)
registry.register(HookPoint.PRE_TOOL_USE, limiter.check, name="rate_limiter", priority=20)
```

**Why per-tool, not global:** a single global counter across every tool punishes a healthy agent
that legitimately uses several different tools in sequence. Per-tool limits catch the actual
failure signature — one tool called in a tight, repetitive loop — without penalizing normal
variety in tool use across a session.

**Failure mode to avoid:** setting the window/limit so tight that legitimate multi-step use of
the same tool (e.g., paginating through search results a few times) gets denied. Calibrate
against real observed usage patterns, not a guessed number — this is the same "measure before
tuning" discipline from
[performance-profiling-guide](../skills/performance-profiling-guide/SKILL.md), applied to a rate
limit instead of a database query.
