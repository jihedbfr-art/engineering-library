# 3. Store platform tokens in Postgres, encrypted, rather than in a secrets manager

Status: accepted

## Context

Every connector needs an access token. Tokens change at runtime — the admin endpoint stores them,
OAuth callbacks refresh them — so they can't live in `application.yml` or in environment variables
baked at deploy time. The two candidates are a proper secrets manager (Vault, AWS Secrets Manager)
or an application-owned table.

## Decision

Keep them in a `platform_credential` table, one row per platform, with the payload encrypted using
AES-GCM. The key comes from `CREDENTIALS_ENC_KEY` (env, base64). Reads and writes go through a
single `CredentialService`; connectors only ever see the `CredentialProvider` port and never touch
storage or crypto directly.

## Consequences

No extra infrastructure to stand up for a single-owner server, and rotating a token is one
authenticated POST. The encryption key still has to be managed outside the app — this moves the
secret from "many tokens" to "one key", which is the point, but it doesn't make the key problem
disappear. For a multi-tenant or higher-assurance deployment a real secrets manager behind the same
`CredentialProvider` port would be the upgrade, and because connectors depend only on that port,
swapping the implementation wouldn't touch them. Documented as a non-goal for v1.
