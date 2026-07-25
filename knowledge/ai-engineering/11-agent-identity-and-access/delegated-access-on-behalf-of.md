# Delegated access — when the agent is acting for a specific human, not for itself

[`agent-identity-pattern.md`](agent-identity-pattern.md) solves machine-to-machine identity: the
agent proves it's the ticket-triage-bot, gets a token scoped to what the ticket-triage-bot is
allowed to do, done. That pattern falls apart the moment the question changes from "what is this
agent allowed to do" to "what is this agent allowed to do *on behalf of Sarah, who asked it to
check her order status just now*." Those are different problems, and conflating them is how an
agent ends up either over-privileged (it can see everyone's orders because its client credentials
can) or unable to do its job (it has no way to prove it's currently acting for Sarah specifically).

## Why client-credentials alone can't answer this

A client-credentials token, by design, says "I am this client" and nothing else. There's no user
in the picture — that's the whole point of the grant, it's built for service-to-service calls with
no human present. The moment an agent needs to fetch *this specific user's* order history, a token
that only proves the agent's own identity is the wrong shape of proof: the downstream order
service can't tell "the triage bot, authorized broadly" from "the triage bot, acting for Sarah
right now, only for Sarah's data." Widening the agent's own scope to cover every user's orders so
it can serve any user is the shortcut teams take here, and it's exactly the over-privileged
outcome per-agent identity was supposed to prevent in the first place.

## The fix: token exchange (RFC 8693)

The agent already holds two things at the moment a user asks it something: its own
client-credentials token (who the agent is) and, if the user is authenticated through the same
IdP, that user's own token or session (who the human is). **Token exchange** lets the agent trade
those for a new token that represents *both*: the user as subject, the agent as actor. Keycloak
implements this as the `urn:ietf:params:oauth:grant-type:token-exchange` grant —
the agent's client presents its own credentials plus the user's `subject_token`, and gets back an
access token scoped to that user, with the agent's client ID carried as the `act` (actor) claim
rather than as the subject.

Two things that make this different from just forwarding the user's token as-is:

- **The downstream service still knows an agent is in the loop.** Forwarding the raw user token
  makes the call indistinguishable from the user clicking a button themselves — useful for an
  audit log question like "did a human or an agent trigger this refund," which token-exchange
  answers and raw forwarding erases.
- **The exchanged token can be scoped down from the user's own scope.** If Sarah's session token
  carries `orders:read orders:write profile:write`, the agent can request an exchange for just
  `orders:read` — it needs to check her order status, not edit her profile — even though nothing
  stops it from requesting more. That's a policy decision made at exchange time, not a technical
  ceiling, so it still has to be enforced deliberately, same as every scope decision elsewhere in
  this library.

## Where this sits next to `keycloak_agent_client.py`

[`keycloak_agent_client.py`](keycloak_agent_client.py)'s `register_agent_client()` and
`get_agent_token()` cover the machine-identity half — a client-credentials token with no user in
it. [`obo_token_exchange.py`](obo_token_exchange.py) covers this file's half: given an already-issued
user token and the agent's own client credentials, it calls the token-exchange endpoint and returns
a token with both identities present, then the same `scoped_token_middleware.py` validator from the
sibling module checks the result — token exchange doesn't need its own enforcement path, it produces
a normal access token that the existing `require_scope` decorator already knows how to check.

```python
def exchange_for_user(
    subject_token: str,
    *,
    requested_scope: str,
    agent: AgentClient,          # the same AgentClient from keycloak_agent_client.py
    base_url: str = KEYCLOAK_URL,
    realm: str = REALM,
) -> str:
    """Trade the agent's own credentials + a user's subject token for a token
    scoped to that user, with the agent recorded as actor (not subject).
    Raises on any non-200 — a failed exchange should block the call, not fall
    back to the agent's own broader client-credentials token.
    """
    resp = requests.post(
        f"{base_url}/realms/{realm}/protocol/openid-connect/token",
        data={
            "grant_type": "urn:ietf:params:oauth:grant-type:token-exchange",
            "client_id": agent.client_id,
            "client_secret": agent.client_secret,
            "subject_token": subject_token,
            "subject_token_type": "urn:ietf:params:oauth:token-type:access_token",
            "requested_token_type": "urn:ietf:params:oauth:token-type:access_token",
            "scope": requested_scope,
        },
        timeout=10,
    )
    resp.raise_for_status()
    return resp.json()["access_token"]
```

## What this doesn't solve

Token exchange assumes the user already authenticated through the same IdP the agent trusts — an
agent fielding requests from a channel with its own separate auth (a Slack bot, a phone-tree IVR)
needs a mapping step from "who Slack says this is" to "who Keycloak says this is" before exchange
is even possible, and that mapping is a real integration project on its own, out of scope here.
Refresh-token-based delegation (the exchanged token expiring while the agent is mid-task and
needing to silently renew it without re-prompting the user) is also left out — it's a real
production concern but adds a second grant type and a token-storage question this file didn't want
to bolt on as an afterthought.

## Where this sits relative to everything else

| Layer | Question it answers | Module |
|---|---|---|
| Delegated access (this file) | Who is this agent acting *for*, right now, and with what subset of their access? | `11-agent-identity-and-access` |
| Agent identity | Is this agent who it claims to be? | [`agent-identity-pattern.md`](agent-identity-pattern.md) |
| Guardrails | Should this text pass through, in either direction? | [`08-guards-safety`](../08-guards-safety/safety-and-guardrails.md) |

Agent identity and delegated access answer related but genuinely different questions — an agent
can be exactly who it claims to be and still be the wrong identity to check *this* answer against,
if the request is really about a specific human it's supposed to be acting for.
