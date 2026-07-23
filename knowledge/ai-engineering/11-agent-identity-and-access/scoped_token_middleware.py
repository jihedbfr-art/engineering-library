"""The enforcement half of agent-identity-pattern.md: a token was issued with
narrow scopes by keycloak_agent_client.py, but nothing stops an agent from
sending it to a call that needs a scope it doesn't have — unless something on
the receiving end actually checks. This is that check.

Fail-closed by default, same rule as the hook and guard-chain modules: any
validation error (expired token, bad signature, unreachable JWKS, missing
scope) blocks the call. There is no fail-open path here on purpose — an
identity check that can be silently bypassed by an outage isn't a check.

Install: pip install -r ../requirements.txt
Run:     python scoped_token_middleware.py
"""

from __future__ import annotations

import functools
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Callable

import jwt
from jwt import PyJWKClient

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import get_logger  # noqa: E402

logger = get_logger(__name__)


class TokenRejected(Exception):
    """Raised whenever a token fails validation or lacks a required scope.

    Deliberately one exception type for every rejection reason (expired,
    bad signature, wrong scope, unreachable IdP) — a caller enforcing this
    middleware shouldn't need to special-case each failure mode, it just
    needs to know the call is blocked and why.
    """


@dataclass
class TokenValidator:
    """Validates a Bearer token against a real Keycloak issuer's published keys.

    issuer looks like 'http://localhost:8080/realms/master'. jwks_client
    caches the signing keys internally and refetches them if a token's `kid`
    isn't in the cache, so this doesn't hit the JWKS endpoint on every call.
    """

    issuer: str
    audience: str | None = None

    def __post_init__(self) -> None:
        self._jwks_client = PyJWKClient(f"{self.issuer}/protocol/openid-connect/certs")

    def validate(self, token: str, *, required_scope: str) -> dict[str, Any]:
        try:
            signing_key = self._jwks_client.get_signing_key_from_jwt(token)
            claims = jwt.decode(
                token,
                signing_key.key,
                algorithms=["RS256"],
                issuer=self.issuer,
                audience=self.audience,
                options={"verify_aud": self.audience is not None},
            )
        except jwt.PyJWTError as exc:
            raise TokenRejected(f"token validation failed: {exc}") from exc
        except Exception as exc:  # noqa: BLE001 - JWKS fetch/network errors land here too
            raise TokenRejected(f"could not validate token (fail-closed): {exc}") from exc

        granted_scopes = set(claims.get("scope", "").split())
        if required_scope not in granted_scopes:
            raise TokenRejected(
                f"token for client '{claims.get('azp', '?')}' has scopes "
                f"{granted_scopes or '{}'}, missing required '{required_scope}'"
            )
        return claims


def require_scope(validator: TokenValidator, scope: str) -> Callable[[Callable], Callable]:
    """Decorator form for gating a tool function an agent calls.

    Expects the wrapped function's first argument to be the Bearer token —
    same shape as the GuardChain.enforce() / hook veto pattern elsewhere in
    this library, so the three enforcement points read the same way in code.
    """

    def decorator(func: Callable) -> Callable:
        @functools.wraps(func)
        def wrapper(token: str, *args: Any, **kwargs: Any) -> Any:
            claims = validator.validate(token, required_scope=scope)
            logger.info("scope '%s' granted to client '%s'", scope, claims.get("azp", "?"))
            return func(token, *args, **kwargs)

        return wrapper

    return decorator


def _make_demo_token(scopes: str, *, expires_in: int, secret: str = "demo-signing-key-thats-long-enough-for-hs256") -> str:
    """Builds a locally HS256-signed token so the demo below runs without a
    live Keycloak instance. Production tokens are RS256, verified against the
    IdP's JWKS via TokenValidator — this helper exists purely so this file's
    __main__ block demonstrates the scope check without a network dependency.
    """
    now = int(time.time())
    return jwt.encode(
        {
            "iss": "demo-issuer",
            "azp": "agent-ticket-triage-bot",
            "scope": scopes,
            "iat": now,
            "exp": now + expires_in,
        },
        secret,
        algorithm="HS256",
    )


def _demo() -> None:
    """Exercises the scope-check logic directly (HS256, no network) rather
    than TokenValidator (RS256, needs a live JWKS endpoint) — see
    keycloak_agent_client.py's _demo() for the end-to-end path against a real
    Keycloak instance.
    """
    secret = "demo-signing-key-thats-long-enough-for-hs256"

    def check(scopes: str, required: str, *, expires_in: int = 300) -> None:
        token = _make_demo_token(scopes, expires_in=expires_in, secret=secret)
        try:
            claims = jwt.decode(token, secret, algorithms=["HS256"])
            granted = set(claims.get("scope", "").split())
            if required not in granted:
                raise TokenRejected(f"missing required scope '{required}', has {granted}")
            print(f"[OK]      scopes={scopes!r} required={required!r}")
        except (TokenRejected, jwt.PyJWTError) as exc:
            print(f"[BLOCKED] scopes={scopes!r} required={required!r} -> {exc}")

    check("tickets:read tickets:comment", "tickets:read")
    check("tickets:read", "tickets:comment")  # missing scope
    check("tickets:read", "tickets:read", expires_in=-10)  # already expired


if __name__ == "__main__":
    _demo()
