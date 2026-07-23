# Hooks — controlling an agent loop from the outside

## The problem hooks solve

The agent loop in [`building-agents.md`](../03-agentic-workflows/building-agents.md) is a
while-loop: call the model, execute a tool if asked, feed the result back, repeat. Once that
loop is running, how do you enforce "never run `rm -rf`", log every tool call for audit, block
a write to a protected file, or inject extra context right before a specific tool runs —
**without editing the agent's core loop or its prompt** every time a new rule shows up?

Hooks are the answer: named extension points in the loop's lifecycle where external code runs
and can observe, modify, or block what happens next. The agent's core loop stays generic and
untouched; policy lives in hooks that plug into it.

## The lifecycle points that matter

Most production agent frameworks converge on roughly the same set of hook points, whatever
they call them:

| Point | Fires | Typical use |
|---|---|---|
| **Session start** | Once, before the first model call | Load context, set up logging, inject a system prompt fragment |
| **Pre-tool-use** | Before a tool call executes | Block a dangerous call, rewrite arguments, require confirmation |
| **Post-tool-use** | After a tool call returns | Log the result, redact secrets before it re-enters context, trigger a side effect |
| **Pre-model-call** | Before each call to the LLM | Inject retrieved context, trim history to fit the budget |
| **On error** | A tool or model call fails | Retry policy, fallback tool, alert |
| **Session end / stop** | The loop finishes | Persist a summary, release resources, final audit log |

Several production coding-agent tools and CI systems already ship a version of this list under
their own naming (pre/post-action hooks, lifecycle callbacks, gate steps) — it's not a
hypothetical design, it's a pattern already carrying real agent traffic in more than one tool.
If you've used a pre-commit hook to block a bad push, or a CI gate to block a risky step, you've
already used this pattern; this file just names it for an LLM agent loop and shows how to build
it yourself for a custom one.

## What makes a hook useful instead of a mess

- **A hook can veto, not just observe.** A logging-only hook is easy; the design gets real once
  a pre-tool-use hook can return "deny" and the loop actually stops the call. That's the
  difference between an audit trail and an enforced guardrail.
- **Hooks compose.** Multiple hooks can register on the same point (a security check and a
  logger both on pre-tool-use) — order matters if any of them can short-circuit the others, so
  the registry needs an explicit priority, not registration order by accident.
- **Hooks are configuration, not code changes.** The whole point is that adding "block writes
  to `.env`" shouldn't require touching the agent's core loop — see
  [`agent_hooks.py`](agent_hooks.py) for a registry that lets policy be added/removed without
  redeploying the loop itself.
- **A hook that can block must fail closed on error.** If a security hook throws an exception,
  the safe default is to deny the action, not silently let it through — the same fail-safe-default
  principle that shows up in any access-control system, applied here to agent tool calls instead
  of HTTP requests.

## Where this fits next to RAG and multi-agent

Hooks are orthogonal to [RAG](../02-rag-architectures/) and
[multi-agent orchestration](../03-agentic-workflows/) — they're not an alternative to either,
they're the control layer that sits around any agent loop regardless of what it's retrieving or
how many agents are involved. A CrewAI crew, a single tool-using agent, and a RAG pipeline with
an agentic re-query step can all use the same hook registry design underneath.

## See also

- [`agent_hooks.py`](agent_hooks.py) — a working `HookRegistry` for a custom loop
- [`skills-pattern.md`](skills-pattern.md) — the complementary pattern for *what an agent knows
  how to do*, where hooks are about *what an agent is allowed to do*
- [`../03-agentic-workflows/building-agents.md`](../03-agentic-workflows/building-agents.md) —
  the bounded loop that hooks attach to
