# Recipe: context-window guard (pre-model-call trim)

**Problem:** a long-running agent loop accumulates messages (tool calls, tool results, prior
reasoning) every step. Left unbounded, the conversation eventually exceeds the model's context
window — or stays under the hard limit but grows large enough to dilute attention and degrade
quality well before hitting it, per the token-budget discussion in
[tokenization-and-embeddings.md](../../01-foundations/tokenization-and-embeddings.md).

**Hook point:** `PRE_MODEL_CALL` — this hook rewrites the payload (the message history about to
be sent) rather than allowing or denying, using the same `modified_payload` mechanism as
[pii-redaction.md](pii-redaction.md).

```python
from agent_hooks import HookContext, HookResult
from shared.utils import estimate_tokens

MAX_CONTEXT_TOKENS = 100_000
KEEP_RECENT_MESSAGES = 6  # always keep the most recent exchange in full, regardless of trimming

def context_window_guard(ctx: HookContext) -> HookResult:
    messages = ctx.payload.get("messages", [])
    total_tokens = sum(estimate_tokens(str(m.get("content", ""))) for m in messages)

    if total_tokens <= MAX_CONTEXT_TOKENS:
        return HookResult()

    # Keep the system/first message (task framing) and the most recent exchanges;
    # summarize or drop the middle — never silently drop the original task framing,
    # that's the one piece of context that makes everything after it interpretable.
    head = messages[:1]
    tail = messages[-KEEP_RECENT_MESSAGES:]
    dropped_count = len(messages) - len(head) - len(tail)

    summary_note = {
        "role": "user",
        "content": f"[{dropped_count} earlier messages omitted to stay within the context budget]",
    }
    trimmed = head + [summary_note] + tail if dropped_count > 0 else messages

    return HookResult(modified_payload={**ctx.payload, "messages": trimmed})
```

**Registration:**
```python
registry.register(HookPoint.PRE_MODEL_CALL, context_window_guard, name="context_guard", priority=50)
```

**Why keep the first message unconditionally:** dropping the original task framing to save space
is a false economy — every message after it becomes harder for the model to interpret correctly
without the goal it was working toward. Trim the middle, never the ends.

**A better alternative for a real production loop:** instead of dropping middle messages
outright, summarize them with a cheap model call and replace them with the summary — preserves
more signal per token than a blunt window cut. The trim-and-note approach above is the simpler,
zero-extra-cost baseline; upgrade to summarization once the blunt version's information loss
actually shows up as a measured quality regression in
[eval_harness.py](../../05-evaluation-observability/eval_harness.py) results, not before.

**Failure mode to avoid:** trimming based on message *count* alone instead of actual token
count — a handful of very long tool results can blow the budget just as fast as many short
messages, and a count-based trim would miss that entirely.
