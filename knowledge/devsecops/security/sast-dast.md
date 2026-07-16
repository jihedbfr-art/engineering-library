# SAST, DAST, SCA — Application Security Testing

## The three families

| | SAST | DAST | SCA |
|---|---|---|---|
| **What** | Static analysis of your source code | Attacks your *running* app from outside | Scans your *dependencies* |
| **Finds** | Injection patterns, hardcoded secrets, unsafe APIs | Real exploitable behavior, config issues | Known CVEs, license issues |
| **Blind to** | Runtime/config issues | Code it can't reach | Your own code |
| **When** | Every commit (fast) | Staging, nightly | Every commit |
| **Tools** | Semgrep, CodeQL, SonarQube | OWASP ZAP, Burp | Trivy, osv-scanner, Dependabot |

You need all three — they overlap almost nowhere.

## SAST: Semgrep quick start

```bash
# Local
semgrep --config p/owasp-top-ten --config p/secrets .

# Custom rule (find dangerous eval in JS)
rules:
  - id: no-eval
    pattern: eval(...)
    message: eval() with dynamic input is code injection
    languages: [javascript]
    severity: ERROR
```

CodeQL (GitHub native) — enable in repo → Security → Code scanning. Free for public repos.

## DAST: OWASP ZAP baseline in CI

```yaml
  dast:
    runs-on: ubuntu-latest
    steps:
      - name: ZAP baseline scan
        uses: zaproxy/action-baseline@v0.12.0
        with:
          target: https://staging.myapp.example
```
- *Baseline* = passive, safe, fast (minutes). 
- *Full scan* = active attacks — **only against your own staging**, never production, never someone else's site.

## SCA: Trivy on everything

```bash
trivy fs .                        # dependencies in the repo
trivy image myapp:1.0             # OS + app packages in the image
trivy config .                    # IaC misconfigurations (Dockerfile, k8s, terraform)
```

## Making it stick (the DevSecOps part)

1. **Block on new CRITICAL/HIGH**, warn on the existing backlog — otherwise teams drown and disable the scanner.
2. Triage findings in the repo (Security tab / SARIF) not in a spreadsheet.
3. Measure **MTTR of vulnerabilities**, not the raw count.
4. False positive? Suppress *with a comment and a reason*, in code review.
