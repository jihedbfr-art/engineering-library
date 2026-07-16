# Secrets Management

I'll be blunt about rule #1 below because I've seen people argue with it: it's not a suggestion. I've watched someone delete a leaked AWS key from a repo, feel relieved, and move on — the key was still valid for another four days because they never rotated it. GitHub's full history (and every fork, every clone, every CI cache) still had it. Rotate first, always, no exceptions.

## The iron rules

1. **A secret that touched git is compromised. Forever.** History doesn't forget — rotate it immediately, don't just delete the file.
2. Secrets don't belong in: code, Dockerfiles, image layers, CI logs, tickets, chat.
3. Every secret must be **rotatable in minutes** and **scoped** (one purpose, least privilege, expiry).

## The hierarchy (worst → best)

| Level | Method | Verdict |
|---|---|---|
| 💀 | Hardcoded in source | Never |
| ⚠️ | `.env` file (git-ignored) | OK for local dev only |
| 🙂 | CI/CD secret store (GitHub Secrets) | Good for pipelines |
| 😀 | Cloud secret manager (AWS/GCP/Azure) | Good in cloud |
| 💪 | Vault (HashiCorp) with dynamic secrets | Best: short-lived, auto-rotated credentials |

## Prevention: block leaks before they happen

```bash
# Pre-commit scanning
pip install pre-commit
# .pre-commit-config.yaml
repos:
  - repo: https://github.com/gitleaks/gitleaks
    rev: v8.18.0
    hooks: [{ id: gitleaks }]
```

In CI (blocking):
```yaml
- name: Scan for secrets
  uses: gitleaks/gitleaks-action@v2
```

Also: enable **GitHub Secret Scanning + Push Protection** on all repos (free for public repos).

## If a secret leaks — incident drill

1. **Rotate first**, investigate second.
2. Check access logs of whatever the secret protected.
3. Purge from history only *after* rotation (`git filter-repo`) — and remember forks/clones may still have it.
4. Post-mortem: how did the scanner miss it?

## Kubernetes specifics

- Native `Secret` objects are base64, **not encrypted** — enable encryption at rest.
- Prefer **External Secrets Operator** or **Sealed Secrets**: the real secret never sits in git.
- Mount as files, not env vars, when possible (env vars leak into `/proc`, crash dumps, child processes).

## Spring Boot example (env-based, no secrets in application.yml)

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

TODO: add a section on rotating secrets for a running Kubernetes deployment without downtime — I keep meaning to write this properly instead of hand-waving "just use External Secrets Operator", since the actual rollout-restart timing has bitten me before.
