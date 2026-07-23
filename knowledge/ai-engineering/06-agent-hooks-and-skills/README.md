# 06 — Agent Hooks & Skills

The two patterns that turned "an LLM with tools" into something you can actually run in
production without babysitting it: **hooks** for control, **skills** for capability.

[hooks-pattern.md](hooks-pattern.md) — named lifecycle points (session start, pre/post-tool-use,
on-error) where external code can observe, rewrite, or veto what an agent loop is about to do.
[agent_hooks.py](agent_hooks.py) is a working `HookRegistry`: fails closed on any hook error,
supports both allow/deny decisions and payload rewriting, runs end-to-end with no API key.

[skills-pattern.md](skills-pattern.md) — packaging a procedure as a discoverable,
lazily-loaded unit instead of stuffing it into a monolithic system prompt, reinvented
convergently across several agent tools because the cost math is unavoidable: context you
don't use for this task shouldn't be paid for on this task. [skill_loader.py](skill_loader.py)
implements the two-phase discovery — cheap frontmatter scan, expensive body load only on match.

Why this module exists: RAG retrieves *facts*, multi-agent orchestration handles *coordination*,
local inference handles *where compute runs* — none of them answer "what is this agent allowed
to do" or "how does it find the right procedure without every procedure living in every prompt."
That's what's actually trending in agent engineering right now, more than another RAG variant.
