"""A hook registry for a custom agent loop: named lifecycle points (session start,
pre/post-tool-use, pre-model-call, on-error, session end) where external code can
observe, modify, or veto what happens next — without touching the loop itself.

See hooks-pattern.md for why this shape (and not just a logging callback) is what
makes hooks useful as a real guardrail layer instead of an audit trail nobody enforces.

This is deliberately framework-agnostic: it doesn't assume LangChain, CrewAI, or any
specific agent loop. Wire HookRegistry.run() calls into whatever loop you're building,
including the pseudocode loop in ../03-agentic-workflows/building-agents.md.
"""

from __future__ import annotations

import sys
from dataclasses import dataclass, field
from enum import Enum
from pathlib import Path
from typing import Any, Callable

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import get_logger  # noqa: E402

logger = get_logger(__name__)


class HookPoint(Enum):
    SESSION_START = "session_start"
    PRE_TOOL_USE = "pre_tool_use"
    POST_TOOL_USE = "post_tool_use"
    PRE_MODEL_CALL = "pre_model_call"
    ON_ERROR = "on_error"
    SESSION_END = "session_end"


class HookDecision(Enum):
    ALLOW = "allow"
    DENY = "deny"


@dataclass
class HookResult:
    decision: HookDecision = HookDecision.ALLOW
    reason: str | None = None
    modified_payload: Any = None  # a hook may rewrite the payload instead of just approving it


@dataclass
class HookContext:
    point: HookPoint
    payload: Any  # e.g. {"tool_name": ..., "arguments": {...}} for PRE_TOOL_USE
    session_id: str
    metadata: dict = field(default_factory=dict)


HookFn = Callable[[HookContext], HookResult]


@dataclass
class _RegisteredHook:
    fn: HookFn
    priority: int
    name: str


class HookRegistry:
    """Register hooks per lifecycle point, run them in priority order, and fail
    closed: if any hook raises or any hook denies, the registry denies. A hook
    that silently swallows its own errors is worse than no hook at all — it gives
    a false sense of enforcement.
    """

    def __init__(self) -> None:
        self._hooks: dict[HookPoint, list[_RegisteredHook]] = {point: [] for point in HookPoint}

    def register(self, point: HookPoint, fn: HookFn, *, name: str | None = None, priority: int = 100) -> None:
        """Lower priority number runs first. Security/veto hooks should generally
        run before observability hooks, so a denied call never reaches a logger
        that assumes it succeeded.
        """
        registered = _RegisteredHook(fn=fn, priority=priority, name=name or fn.__name__)
        self._hooks[point].append(registered)
        self._hooks[point].sort(key=lambda h: h.priority)
        logger.debug("registered hook '%s' on %s (priority=%d)", registered.name, point.value, priority)

    def run(self, context: HookContext) -> HookResult:
        current_payload = context.payload
        for hook in self._hooks[context.point]:
            try:
                result = hook.fn(HookContext(
                    point=context.point,
                    payload=current_payload,
                    session_id=context.session_id,
                    metadata=context.metadata,
                ))
            except Exception as exc:  # noqa: BLE001 - fail closed, see docstring
                logger.error("hook '%s' raised, denying by default: %s", hook.name, exc)
                return HookResult(decision=HookDecision.DENY, reason=f"hook '{hook.name}' errored: {exc}")

            if result.decision is HookDecision.DENY:
                logger.warning("hook '%s' denied %s: %s", hook.name, context.point.value, result.reason)
                return result

            if result.modified_payload is not None:
                current_payload = result.modified_payload

        return HookResult(decision=HookDecision.ALLOW, modified_payload=current_payload)


# --- Example hooks -----------------------------------------------------------

DANGEROUS_COMMAND_PREFIXES = ("rm -rf", "git push --force", "DROP TABLE", "DROP DATABASE")


def block_dangerous_shell_commands(ctx: HookContext) -> HookResult:
    """A pre-tool-use veto hook — the concrete example from hooks-pattern.md."""
    if ctx.payload.get("tool_name") != "run_shell":
        return HookResult()  # not our tool, allow and let other hooks decide

    command = ctx.payload.get("arguments", {}).get("command", "")
    for prefix in DANGEROUS_COMMAND_PREFIXES:
        if command.strip().startswith(prefix):
            return HookResult(decision=HookDecision.DENY, reason=f"command starts with blocked prefix: {prefix!r}")
    return HookResult()


def log_tool_use(ctx: HookContext) -> HookResult:
    """A pure observability hook — runs after the veto hooks (higher priority number)."""
    logger.info("tool call: %s(%s)", ctx.payload.get("tool_name"), ctx.payload.get("arguments"))
    return HookResult()


def redact_secrets_from_tool_output(ctx: HookContext) -> HookResult:
    """A post-tool-use hook that rewrites the payload before it re-enters context —
    demonstrates modified_payload, not just allow/deny.
    """
    output = str(ctx.payload.get("output", ""))
    for marker in ("sk-", "AKIA", "ghp_"):
        if marker in output:
            redacted = output.replace(marker, "[REDACTED]")
            return HookResult(modified_payload={**ctx.payload, "output": redacted})
    return HookResult()


def _demo() -> None:
    registry = HookRegistry()
    registry.register(HookPoint.PRE_TOOL_USE, block_dangerous_shell_commands, priority=10)
    registry.register(HookPoint.PRE_TOOL_USE, log_tool_use, priority=50)
    registry.register(HookPoint.POST_TOOL_USE, redact_secrets_from_tool_output, priority=10)

    safe_call = HookContext(
        point=HookPoint.PRE_TOOL_USE,
        payload={"tool_name": "run_shell", "arguments": {"command": "ls -la"}},
        session_id="demo-session",
    )
    print("safe call ->", registry.run(safe_call))

    dangerous_call = HookContext(
        point=HookPoint.PRE_TOOL_USE,
        payload={"tool_name": "run_shell", "arguments": {"command": "rm -rf /"}},
        session_id="demo-session",
    )
    print("dangerous call ->", registry.run(dangerous_call))

    leaky_output = HookContext(
        point=HookPoint.POST_TOOL_USE,
        payload={"tool_name": "read_file", "output": "API_KEY=sk-abc123"},
        session_id="demo-session",
    )
    print("post-tool redaction ->", registry.run(leaky_output))


if __name__ == "__main__":
    _demo()
