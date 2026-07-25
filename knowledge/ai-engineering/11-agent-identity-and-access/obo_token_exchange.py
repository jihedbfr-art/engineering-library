"""On-behalf-of token exchange (RFC 8693) — the lab for
delegated-access-on-behalf-of.md. Given the agent's own client credentials plus
an already-issued user access token, trades both for a new token that carries
the user as subject and the agent as actor, scoped down to whatever the agent
actually needs for this one call.

Needs the same running Keycloak instance as keycloak_agent_client.py, an agent
client already registered there (see that file), and a real user access token
to exchange (obtained however this realm's users normally log in — a resource-
owner password grant against a test user is the least-friction way to get one
locally; production code should never use that grant for anything else).

Run:  python obo_token_exchange.py
"""

from __future__ import annotations

import sys
from pathlib import Path

import requests

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import get_logger  # noqa: E402

from keycloak_agent_client import KEYCLOAK_URL, REALM, AgentClient  # noqa: E402

logger = get_logger(__name__)


def exchange_for_user(
    subject_token: str,
    *,
    requested_scope: str,
    agent: AgentClient,
    base_url: str = KEYCLOAK_URL,
    realm: str = REALM,
) -> str:
    """Trade the agent's own credentials + a user's subject token for a token
    scoped to that user, with the agent recorded as actor (not subject).

    Raises on any non-200 response on purpose — a failed exchange should block
    the call, not silently fall back to the agent's own broader
    client-credentials token, which would defeat the point of scoping down.
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
    token = resp.json()["access_token"]
    logger.info(
        "exchanged token for agent '%s' scoped to '%s'", agent.client_id, requested_scope
    )
    return token


def get_user_token_via_password_grant(
    username: str,
    password: str,
    *,
    client_id: str = "admin-cli",
    base_url: str = KEYCLOAK_URL,
    realm: str = REALM,
) -> str:
    """Resource-owner password grant, for getting a real user token locally to
    exchange against. This grant type is deprecated for anything user-facing —
    it's here only because a demo script needs some way to obtain a starting
    user token without standing up a full browser login flow.
    """
    resp = requests.post(
        f"{base_url}/realms/{realm}/protocol/openid-connect/token",
        data={
            "grant_type": "password",
            "client_id": client_id,
            "username": username,
            "password": password,
        },
        timeout=10,
    )
    resp.raise_for_status()
    return resp.json()["access_token"]


def _demo() -> None:
    print(
        "This demo needs an agent client already registered via "
        "keycloak_agent_client.py and a real test-user account in the same "
        "realm — token exchange has nothing to exchange without a genuine "
        "user token to start from, so this isn't runnable end-to-end without "
        "that setup. The functions above (exchange_for_user, "
        "get_user_token_via_password_grant) are the real, runnable pieces; "
        "wire them together against your own Keycloak realm and test user."
    )


if __name__ == "__main__":
    _demo()
