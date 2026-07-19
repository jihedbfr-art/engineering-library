# 03 — Agentic Workflows

[building-agents.md](building-agents.md) — what an agent actually is (an LLM in a bounded
loop with tools), the tool-design rules that matter more than which framework you pick, and
when multi-agent is worth its cost versus one agent with good tools.

[single_agent_tool_loop.py](single_agent_tool_loop.py) is the baseline the doc argues for
first: one agent, a couple of narrow tools (a safe arithmetic evaluator — never a raw `eval()` —
and a clock), a bounded step loop, and a `HookRegistry` from
[06-agent-hooks-and-skills](../06-agent-hooks-and-skills/) wired into the pre-tool-use step. Start
here before reaching for a crew.

[research_agent_crew.py](research_agent_crew.py) puts the multi-agent escalation into practice: a
Researcher agent and a Writer agent, built with CrewAI, handing work off sequentially. The
researcher has a free DuckDuckGo search tool (no paid API key needed to run this yourself);
the writer only sees the researcher's verified output, never the raw web results — that
boundary is deliberate, it's what keeps the writer from inventing claims the research didn't
support.

Both files apply the same bounds discipline: max steps, max requests per minute, explicit
tool schemas. An unbounded agent loop is not a demo feature, it's an incident waiting to bill
you for it.
