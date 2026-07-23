"""Registers a per-agent OAuth2 confidential client in Keycloak and fetches a
scoped token for it — the lab for agent-identity-pattern.md's "one client per
agent" pattern.

Three steps, each a small function so you can call them independently from a
notebook or a provisioning script instead of only via _demo():
  1. get_admin_token()      — authenticate as realm admin
  2. register_agent_client() — create a confidential, service-account-only client
  3. get_agent_token()       — client-credentials grant for that new client

Needs a running Keycloak instance, no cloud API key:
  docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
      quay.io/keycloak/keycloak:26.0 start-dev

Run:  python keycloak_agent_client.py
"""

from __future__ import annotations

import sys
from dataclasses import dataclass
from pathlib import Path

import requests

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import ConfigError, get_logger, require_env  # noqa: E402

logger = get_logger(__name__)

KEYCLOAK_URL = "http://localhost:8080"
REALM = "master"


@dataclass
class AgentClient:
    client_id: str
    client_secret: str
    scopes: list[str]


def get_admin_token(base_url: str = KEYCLOAK_URL) -> str:
    """Authenticate against the master realm's admin-cli client.

    In a real deployment the admin username/password come from a secrets
    manager, not a default — the 'admin'/'admin' fallback here only exists so
    this demo runs against the throwaway `start-dev` container in the
    docstring above. Set KEYCLOAK_ADMIN_USER / KEYCLOAK_ADMIN_PASSWORD to
    override, and never rely on the fallback outside a local demo.
    """
    resp = requests.post(
        f"{base_url}/realms/master/protocol/openid-connect/token",
        data={
            "grant_type": "password",
            "client_id": "admin-cli",
            "username": require_env("KEYCLOAK_ADMIN_USER", default="admin"),
            "password": require_env("KEYCLOAK_ADMIN_PASSWORD", default="admin"),
        },
        timeout=10,
    )
    resp.raise_for_status()
    return resp.json()["access_token"]


def register_agent_client(
    admin_token: str,
    agent_name: str,
    scopes: list[str],
    *,
    base_url: str = KEYCLOAK_URL,
    realm: str = REALM,
) -> AgentClient:
    """Create a confidential, service-account-only client for one agent.

    standardFlowEnabled=False because this client never does an interactive
    user login — it's client-credentials only, exactly the machine identity
    described in agent-identity-pattern.md. directAccessGrantsEnabled=False
    for the same reason: no resource-owner password grant, no interactive flow.
    """
    client_id = f"agent-{agent_name}"
    headers = {"Authorization": f"Bearer {admin_token}"}

    create_resp = requests.post(
        f"{base_url}/admin/realms/{realm}/clients",
        headers=headers,
        json={
            "clientId": client_id,
            "protocol": "openid-connect",
            "publicClient": False,
            "standardFlowEnabled": False,
            "directAccessGrantsEnabled": False,
            "serviceAccountsEnabled": True,
            "attributes": {"agent.scopes": ",".join(scopes)},
        },
        timeout=10,
    )
    if create_resp.status_code not in (201, 409):  # 409 = already exists, treat as OK for a demo
        create_resp.raise_for_status()

    lookup = requests.get(
        f"{base_url}/admin/realms/{realm}/clients",
        headers=headers,
        params={"clientId": client_id},
        timeout=10,
    )
    lookup.raise_for_status()
    matches = lookup.json()
    if not matches:
        raise ConfigError(f"client '{client_id}' was not found after creation")
    internal_id = matches[0]["id"]

    secret_resp = requests.get(
        f"{base_url}/admin/realms/{realm}/clients/{internal_id}/client-secret",
        headers=headers,
        timeout=10,
    )
    secret_resp.raise_for_status()
    client_secret = secret_resp.json()["value"]

    logger.info("registered client '%s' with scopes %s", client_id, scopes)
    return AgentClient(client_id=client_id, client_secret=client_secret, scopes=scopes)


def get_agent_token(agent: AgentClient, *, base_url: str = KEYCLOAK_URL, realm: str = REALM) -> str:
    """Client-credentials grant — the agent authenticating as itself, no user in the loop."""
    resp = requests.post(
        f"{base_url}/realms/{realm}/protocol/openid-connect/token",
        data={
            "grant_type": "client_credentials",
            "client_id": agent.client_id,
            "client_secret": agent.client_secret,
        },
        timeout=10,
    )
    resp.raise_for_status()
    return resp.json()["access_token"]


def _demo() -> None:
    try:
        admin_token = get_admin_token()
    except requests.exceptions.ConnectionError:
        print(
            "Could not reach Keycloak at "
            f"{KEYCLOAK_URL} — start it first (see the docker run command in this "
            "file's docstring), then re-run."
        )
        return

    agent = register_agent_client(
        admin_token,
        agent_name="ticket-triage-bot",
        scopes=["tickets:read", "tickets:comment"],
    )
    print(f"registered {agent.client_id} — client_secret hidden, scopes: {agent.scopes}")

    token = get_agent_token(agent)
    print(f"fetched access token ({len(token)} chars) — pass this as a Bearer token")
    print("verify it with scoped_token_middleware.py's require_scope('tickets:read')")


if __name__ == "__main__":
    _demo()
