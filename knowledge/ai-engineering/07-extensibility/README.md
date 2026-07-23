# 07 — Extensibility (Plugins & Connectors)

The layer below [skills](../06-agent-hooks-and-skills/skills-pattern.md): not *what procedures*
an agent knows, but *what external systems* it can reach — calendars, ticketing systems,
internal APIs, databases — without those integrations being hard-coded into the agent itself.

[plugin-architecture.md](plugin-architecture.md) covers the pattern: a manifest declaring tools
and required permissions, a connection mechanism (local process or networked service), and —
the part most often shortchanged — a permission boundary that's actually enforced, not just
documented as a suggestion in a README nobody re-reads.

[plugin_registry.py](plugin_registry.py) makes that boundary concrete: discovers plugin
manifests, and deliberately separates what a plugin *requests* from what it's actually
*granted* — the demo connects a plugin that asks for read+write calendar access, grants only
read, and shows the write call getting denied at call time, not just documented as unsupported.

Four extension mechanisms now live in this library, each answering a different question:

| Mechanism | Answers |
|---|---|
| [Plugins/connectors](plugin-architecture.md) (this module) | What external systems can the agent reach? |
| [Skills](../06-agent-hooks-and-skills/skills-pattern.md) | What procedures does the agent know? |
| [Hooks](../06-agent-hooks-and-skills/hooks-pattern.md) | What is the agent allowed to do, right now? |
| [RAG](../02-rag-architectures/) | What facts does the agent need for this question? |

None of the four substitutes for another — a real production agent composes all four.
