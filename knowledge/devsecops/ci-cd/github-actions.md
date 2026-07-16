# GitHub Actions — Secure CI/CD Pipeline

## Anatomy of a workflow

```yaml
name: ci
on:
  push:
    branches: [main]
  pull_request:

permissions:            # least privilege — always declare
  contents: read

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
          cache: maven
      - run: mvn -B verify
```

## The security checklist

| Rule | Why |
|---|---|
| Pin actions to a full SHA (`actions/checkout@8ade135...`) | Tags can be moved by attackers |
| `permissions:` block on every workflow | Default token is too powerful |
| Never `pull_request_target` + checkout of PR code | Classic RCE on your secrets |
| Secrets via `secrets.*`, never hardcoded | Leaks live forever in git history |
| Separate deploy job with `environment:` + required reviewers | Human gate before prod |
| Add dependency scanning (Dependabot / `dependency-review-action`) | Supply-chain defense |

## A complete DevSecOps pipeline (stages)

```
lint → unit tests → SAST (CodeQL/Semgrep) → build image
→ image scan (Trivy) → SBOM (Syft) → sign (Cosign)
→ deploy to staging → DAST (ZAP baseline) → manual gate → prod
```

### SAST with Semgrep

```yaml
  sast:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: returntocorp/semgrep-action@v1
        with:
          config: p/owasp-top-ten
```

### Image scan with Trivy

```yaml
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: aquasecurity/trivy-action@master
        with:
          image-ref: ghcr.io/${{ github.repository }}:${{ github.sha }}
          exit-code: '1'          # fail the build on HIGH/CRITICAL
          severity: HIGH,CRITICAL
```

## Caching & speed

- Cache dependencies (`actions/cache`, or `cache:` input of setup-* actions).
- Run independent jobs in parallel; use `needs:` only when order matters.
- Use matrix builds for multi-version testing.

## Golden rules

1. A pipeline that isn't blocking on security findings is decoration.
2. Build once, promote the same artifact through environments.
3. Every secret in CI should be rotatable in minutes.
