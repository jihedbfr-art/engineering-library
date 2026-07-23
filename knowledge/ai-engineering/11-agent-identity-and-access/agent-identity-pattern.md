# Agent Identity & Access — Every Agent Gets Its Own Client, Not a Shared Key

Every module before this one assumes the agent can already reach the model, the vector store, the
tools it needs. None of them ask *who* is calling on the agent's behalf, or what happens once
there are five agents instead of one. This module is about that gap, and it's the one I see
skipped most often in real deployments — probably because it's an identity problem, not an AI
problem, and it lands on whoever set up the LLM API keys rather than whoever owns IAM.

## The problem: one API key, shared everywhere

The default a team backs into: a single API key (or a single service-account credential) gets
generated once, dropped into a `.env` file, and every agent, script, and cron job in the
organization ends up using it. It works right up until something goes wrong — an agent
misbehaves, a key leaks in a log line, someone needs to know which of twelve agents made a
specific call last Tuesday — and the answer to "who did this" is "the shared key," which is not
an answer.

It also means every agent has the exact same permissions as every other agent, because there's
only one identity to grant permissions to. A read-only reporting agent and a write-capable
remediation agent end up indistinguishable to whatever system they're calling into.

## The pattern: one OAuth2/OIDC client per agent

Register each agent as its own **client** in an identity provider — Keycloak, in the examples
here, because it's self-hostable and the Admin REST API makes scripting client creation
straightforward, but the pattern holds for any OIDC-compliant IdP. Concretely:

- **Confidential client, not public.** The agent runs server-side (or in a controlled
  environment), so it can hold a client secret. `standardFlowEnabled=false`,
  `serviceAccountsEnabled=true` — this client never does an interactive user login, it authenticates
  as itself via the client-credentials grant.
- **Scopes mapped to what the agent's tools actually need**, not a blanket "agent" role. If an
  agent has three tools — read a ticket queue, post a comment, escalate to a human — that's three
  scopes, and the client only gets the ones its tool set actually requires. This is the same
  least-privilege instinct as
  [`../07-extensibility/plugin-architecture.md`](../07-extensibility/plugin-architecture.md)'s
  plugin manifest, applied to the agent's own identity instead of to a plugin it connects to.
- **Short-lived access tokens.** Client-credentials tokens expire in minutes, not months. The
  agent re-requests a token when the old one expires; it never holds a long-lived bearer token
  that keeps working if it's exfiltrated six weeks later. The client *secret* is the long-lived
  thing here, and it belongs in a secrets manager — never in the same repo as the agent code,
  never in a log line.

[`keycloak_agent_client.py`](keycloak_agent_client.py) scripts exactly this: authenticate as an
admin, register a new confidential client for a named agent with a given scope list, then fetch a
token for that client and show what's actually inside it.

## Why this holds up under a bad day

The point of per-agent identity isn't audit-log tidiness, though you get that for free. It's blast
radius. If one agent's credentials get compromised — a prompt-injection attack that tricks it into
leaking its own token, a bug that logs the bearer header, whatever — the damage is bounded by that
one client's scopes and that one token's few-minute lifetime. Compare that to a shared key: rotate
it and every agent breaks simultaneously; don't rotate it and the compromised scope is
everything, forever.

I've watched a team debug "why did the reporting bot delete records" for the better part of a day
before realizing the reporting bot and the cleanup job shared credentials, and the log line just
said which key was used, not which process was holding it at the time. Per-agent clients make that
question answerable by construction — the token itself carries the client ID.

## Enforcing scopes, not just issuing them

Issuing a narrowly-scoped token is only half the pattern. Something on the receiving end has to
actually check the scope before doing the privileged thing, or the narrow scope is decoration.
[`scoped_token_middleware.py`](scoped_token_middleware.py) is that check: validate the token's
signature and expiry against the IdP's published keys, confirm the required scope is present, and
**fail closed** — any validation error (expired token, missing scope, unreachable JWKS endpoint)
blocks the call rather than letting it through. Same fail-closed default as the pre-tool-use hook
in [`06-agent-hooks-and-skills/hooks-pattern.md`](../06-agent-hooks-and-skills/hooks-pattern.md)
and the guard chain in
[`08-guards-safety/safety-and-guardrails.md`](../08-guards-safety/safety-and-guardrails.md) — this
module is a third instance of the same rule applied to a third kind of gate.

## Where this sits relative to everything else

| Layer | Question it answers | Module |
|---|---|---|
| Identity & access (this module) | Is this agent who it claims to be, and is it allowed to do *this specific thing*? | `11-agent-identity-and-access` |
| Guardrails | Should this text pass through, in either direction? | [`08-guards-safety`](../08-guards-safety/safety-and-guardrails.md) |
| Hooks | Should this action execute right now? | [`06-agent-hooks-and-skills`](../06-agent-hooks-and-skills/hooks-pattern.md) |
| Plugin permissions | What can this specific tool touch? | [`07-extensibility`](../07-extensibility/plugin-architecture.md) |

Identity answers a question none of the other three can: those three all assume you already know
which agent is asking. This module is what makes that assumption true instead of aspirational.

TODO: this only covers client-credentials (machine-to-machine). An agent acting *on behalf of a
specific human user* — token exchange, on-behalf-of flows — is a different enough problem
(delegation, not just service identity) that it deserves its own file rather than a paragraph
bolted onto this one. Left out for now rather than done badly.
