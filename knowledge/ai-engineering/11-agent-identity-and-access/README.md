# 11 — Agent Identity & Access

The layer that answers "who is this agent, and is it allowed to do *this specific thing*?" —
a question every other module in this library quietly assumes is already handled.
[`agent-identity-pattern.md`](agent-identity-pattern.md) is the course: why a single shared API
key across every agent is the default that eventually bites someone, why the fix is one OAuth2/OIDC
confidential client per agent with its own scopes, and why short-lived tokens matter more than
they look like they should.

## What this module ships

- [`keycloak_agent_client.py`](keycloak_agent_client.py) — the lab. Authenticates as a Keycloak
  admin, registers a confidential, service-account-only client for a named agent with a specific
  scope list, and fetches a client-credentials token for it. Run it against a local Keycloak and
  you have a real per-agent identity, not a diagram of one.
- [`scoped_token_middleware.py`](scoped_token_middleware.py) — the enforcement side. Validates a
  Bearer token against the issuer's published keys and checks the required scope is present,
  fail-closed on every error path (expired token, bad signature, unreachable JWKS, missing scope).

## Quick start

```bash
# needs a running Keycloak instance, no cloud API key:
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
    quay.io/keycloak/keycloak:26.0 start-dev

python keycloak_agent_client.py       # registers agent-ticket-triage-bot, fetches its token
python scoped_token_middleware.py     # scope-check demo, runs offline (HS256, no live IdP needed)
```

## Where this sits relative to everything else

| Layer | Question it answers | Module |
|---|---|---|
| Identity & access (here) | Is this agent who it claims to be, and is it allowed to do *this*? | `11-agent-identity-and-access` |
| Guardrails | Should this text pass through, in either direction? | [`08-guards-safety`](../08-guards-safety/safety-and-guardrails.md) |
| Hooks | Should this action execute right now? | [`06-agent-hooks-and-skills`](../06-agent-hooks-and-skills/hooks-pattern.md) |
| Plugin permissions | What can this specific tool touch? | [`07-extensibility`](../07-extensibility/plugin-architecture.md) |

None of the four replace each other — a perfectly-scoped token can still front a tool call a hook
vetoes for unrelated reasons, and a guardrail can still block the resulting text. Identity is the
layer that makes "which agent is this" an answerable, enforced question instead of a log-line
guess.
