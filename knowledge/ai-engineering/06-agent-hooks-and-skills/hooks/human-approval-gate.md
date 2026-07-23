# Recipe: human approval gate (pause for confirmation on irreversible actions)

**Problem:** [agent-system-prompts.md](../../01-foundations/agent-system-prompts.md) argues an
agent's system prompt should state an authorization boundary in words — but a prompt instruction
is a strong suggestion the model can still misjudge under an ambiguous or adversarial situation.
For genuinely irreversible actions (send an email, delete data, deploy to production, spend real
money), the boundary needs to be a structural pause, not just wording the model is trusted to
honor every time.

**Hook point:** `PRE_TOOL_USE` — this hook's `HookResult` alone can't literally block on a human
in a synchronous function call in most loop designs, so the practical pattern is: deny by
default and require a separate, explicit approval step to have already recorded consent before
the call is allowed through.

```python
from dataclasses import dataclass, field
from agent_hooks import HookContext, HookDecision, HookResult

IRREVERSIBLE_TOOLS = {"send_email", "delete_record", "deploy", "charge_payment"}

@dataclass
class ApprovalGate:
    approved_call_ids: set[str] = field(default_factory=set)

    def request_approval(self, tool_name: str, args: dict, call_id: str) -> None:
        """Called by the surrounding application, outside the hook itself — e.g. to
        surface a confirmation prompt in a UI or CLI. Approval is recorded separately
        from the loop's own execution so a human genuinely has to act, not just the
        loop assuming consent.
        """
        print(f"\n[APPROVAL NEEDED] {tool_name}({args}) — call_id={call_id}")
        response = input("Approve? [y/N]: ").strip().lower()
        if response == "y":
            self.approved_call_ids.add(call_id)

    def gate(self, ctx: HookContext) -> HookResult:
        tool_name = ctx.payload.get("tool_name", "")
        if tool_name not in IRREVERSIBLE_TOOLS:
            return HookResult()  # not a gated action, allow

        call_id = ctx.payload.get("call_id", "")
        if call_id in self.approved_call_ids:
            return HookResult()  # explicit prior approval recorded for this exact call

        return HookResult(
            decision=HookDecision.DENY,
            reason=f"'{tool_name}' requires human approval before it can execute; none recorded for this call",
        )
```

**Registration:**
```python
gate = ApprovalGate()
registry.register(HookPoint.PRE_TOOL_USE, gate.gate, name="human_approval_gate", priority=10)
# the surrounding application calls gate.request_approval(...) and, on approval,
# re-attempts the same tool call — which now passes the gate
```

**Why deny-then-retry instead of blocking inline:** blocking synchronously inside a hook
function assumes a human is available to answer immediately in whatever thread the loop runs on
— rarely true in a real deployment (a chat UI, a queued background job). Denying with a clear
reason and letting the surrounding application re-drive the same call after recording approval
keeps the hook itself simple and keeps the approval UX decision (a CLI prompt, a Slack message, a
web form) out of the hook's responsibility entirely.

**Failure mode to avoid:** keying approval on `tool_name` alone rather than a specific call
identifier — approving one `delete_record` call should not silently pre-approve every subsequent
`delete_record` call in the same session with different arguments. Each irreversible call needs
its own explicit approval.
