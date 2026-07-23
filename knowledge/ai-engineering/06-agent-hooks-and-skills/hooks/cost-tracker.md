# Recipe: cost tracker (token/call accounting per session)

**Problem:** an agent loop with no bound on how many model calls or tool calls it makes can run
away in cost — not through malice, just through a task that turns out to need more steps than
expected, or a loop that never quite converges. `max_iter`/`MAX_STEPS` bounds the *step count*;
this hook bounds the *actual cost*, which isn't always proportional to steps (one step can be a
cheap tool call, another can be an expensive model call with a huge context).

**Hook points:** `PRE_MODEL_CALL` (check budget before spending more), `POST_TOOL_USE`/after each
model response (accumulate actual cost).

```python
from dataclasses import dataclass, field
from agent_hooks import HookContext, HookDecision, HookResult

@dataclass
class CostTracker:
    max_cost_usd: float
    price_per_1k_input_tokens: float
    price_per_1k_output_tokens: float
    accumulated_usd: float = field(default=0.0)

    def pre_model_call_gate(self, ctx: HookContext) -> HookResult:
        if self.accumulated_usd >= self.max_cost_usd:
            return HookResult(
                decision=HookDecision.DENY,
                reason=f"session cost budget exhausted: ${self.accumulated_usd:.4f} >= ${self.max_cost_usd:.4f}",
            )
        return HookResult()

    def record_usage(self, input_tokens: int, output_tokens: int) -> None:
        cost = (input_tokens / 1000) * self.price_per_1k_input_tokens + \
               (output_tokens / 1000) * self.price_per_1k_output_tokens
        self.accumulated_usd += cost
```

**Registration:**
```python
tracker = CostTracker(max_cost_usd=0.50, price_per_1k_input_tokens=0.003, price_per_1k_output_tokens=0.015)
registry.register(HookPoint.PRE_MODEL_CALL, tracker.pre_model_call_gate, name="cost_gate", priority=10)
# call tracker.record_usage(...) after each model response using the provider's actual reported token counts
```

**Why deny on a hard budget instead of just warning:** a warning that nobody's watching in real
time doesn't stop the spend — for an unattended/autonomous agent run, the budget has to be an
actual gate, the same fail-closed principle from
[hooks-pattern.md](../hooks-pattern.md#what-makes-a-hook-useful-instead-of-a-mess), not a log
line reviewed after the bill arrives.

**Failure mode to avoid:** estimating cost from token counts you compute yourself instead of the
provider's actual reported usage — tokenization details vary enough between an estimate and the
real count that a budget gate built on estimates can be meaningfully wrong in either direction.
Use the real usage numbers returned with each response wherever the API provides them.
