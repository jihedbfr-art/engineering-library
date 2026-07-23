# Plugins & Connectors — Extending an Agent Without Redeploying It

[skills-pattern.md](../06-agent-hooks-and-skills/skills-pattern.md) covers packaging
*procedures*. This file covers the layer below that: packaging *capabilities* — external tools,
data sources, and services — as discoverable, independently-versioned units an agent connects to
at runtime, instead of tools hard-coded into the agent's own codebase.

## The problem this pattern solves

Every agent framework eventually hits the same wall: tools defined inline in the agent's code
mean every new integration (a ticketing system, a database, a search index) requires touching
and redeploying the agent itself. That doesn't scale past a handful of tools, and it means the
team building the agent has to also own every integration it might ever need — usually the wrong
team for that job.

A **plugin** (also called a connector, or a tool server depending on the ecosystem) inverts this:
it's a separate process or service that exposes a set of tools/resources through a standard
protocol, and the agent connects to it at runtime rather than importing its code. The team that
owns the ticketing system can own and deploy the ticketing plugin independently of whoever owns
the agent — the same separation of concerns that made microservices useful for regular backend
systems, applied to agent tooling.

## What a plugin actually needs to expose

Regardless of the specific protocol, the shape converges on the same three things:

1. **A manifest** — machine-readable metadata: what tools/resources this plugin provides, their
   schemas (same shape as the tool schemas in
   [`../03-agentic-workflows/building-agents.md`](../03-agentic-workflows/building-agents.md)),
   and what permissions it requires to operate.
2. **A connection mechanism** — how the agent's runtime actually talks to it: often a local
   process communicating over stdio, or a network service over HTTP/a similar RPC transport.
   Local-process plugins are simpler to develop and don't need network exposure; networked
   plugins scale to shared infrastructure multiple agents can connect to simultaneously.
3. **A permission/consent boundary** — connecting to a plugin should not silently grant it
   unlimited access to everything the agent can do. The manifest should declare what the plugin
   needs (read-only database access, a specific API scope), and the runtime should be able to
   enforce that scope, not just document it as a suggestion.

## Plugins vs. skills vs. hooks vs. RAG — four different levers

| Mechanism | Extends... | Deployed... |
|---|---|---|
| **Plugin/connector** | What external systems the agent can reach | As an independent process/service |
| **Skill** | What procedures the agent knows | As a file bundled with the agent or discovered at runtime |
| **Hook** | What the agent is allowed to do at each step | As policy code wired into the loop itself |
| **RAG** | What facts the agent has for a specific question | As an index queried per request |

They compose: a plugin can expose a tool, a skill can tell the agent *when and how* to use that
tool well, and a hook can gate whether a specific call to it is actually allowed to execute this
time. None of the four replaces the others.

## Security model — the part most often shortchanged

A plugin is, functionally, third-party code (or a third-party service) the agent grants tool
access to. Treat every plugin connection with the same scrutiny as adding a new dependency to a
codebase, not as a convenience to enable and forget:

- **Least privilege by default** — a plugin that only needs to read a calendar shouldn't be
  granted write access "in case it's useful later." Grant the narrowest scope the manifest
  actually requires.
- **Version pinning** — a plugin that auto-updates without review can change its behavior (or its
  requested permissions) underneath an agent that was only ever reviewed against an earlier
  version.
- **Treat plugin output as untrusted input**, same as any tool result — a compromised or buggy
  plugin returning malicious content into the agent's context is the same prompt-injection risk
  surface as untrusted web content or file content, covered in
  [`llm-fundamentals.md`](../01-foundations/llm-fundamentals.md)'s security section.
- **A pre-tool-use hook is still the right enforcement point** for a specific dangerous call from
  a plugin's tool, exactly as it would be for a locally-defined tool — see
  [`../06-agent-hooks-and-skills/hooks/security-gate.md`](../06-agent-hooks-and-skills/hooks/security-gate.md).
  A plugin boundary is a *capability* boundary (what it could theoretically call); a hook is a
  *policy* boundary (what it's allowed to call right now) — both matter, neither replaces the
  other.

## See also

- [`plugin_registry.py`](plugin_registry.py) — a working registry that discovers plugin
  manifests, validates their declared permission scope, and exposes their tools with that scope
  enforced
- [`../06-agent-hooks-and-skills/hooks-pattern.md`](../06-agent-hooks-and-skills/hooks-pattern.md) —
  the policy-enforcement layer that sits between "the plugin could do this" and "the plugin is
  allowed to do this right now"
