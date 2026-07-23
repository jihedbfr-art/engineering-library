# OWASP Top 10 (2021) — With Real Fixes

The ten most critical web application security risks, and how to actually close them.

## A01 — Broken Access Control
The #1 risk. Users acting outside their intended permissions.
- **Symptom**: changing `/api/users/42` to `/api/users/43` shows someone else's data (IDOR).
- **Fix**: enforce authorization **server-side on every request**, deny by default, test with a low-privilege account. Never rely on hiding buttons.

## A02 — Cryptographic Failures
- Passwords: **bcrypt / argon2**, never MD5/SHA-1, never reversible encryption.
- TLS everywhere (including service-to-service). HSTS on.
- Don't invent crypto. Don't store data you don't need.

## A03 — Injection (SQL, NoSQL, OS, LDAP)
```java
// VULNERABLE
stmt.execute("SELECT * FROM users WHERE name = '" + input + "'");

// SAFE — parameterized
PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE name = ?");
ps.setString(1, input);
```
- ORMs help but raw fragments (`@Query` with string concat) reintroduce it.
- Same principle for OS commands: never build shell strings from user input.

## A04 — Insecure Design
Security starts before code: threat modeling (STRIDE), abuse cases, rate limits on sensitive flows (login, OTP, password reset), business-logic review.

## A05 — Security Misconfiguration
- Default credentials, open cloud buckets, verbose stack traces in prod, unnecessary features enabled.
- **Fix**: hardened baseline images/configs, automated config scanning, minimal footprint.

## A06 — Vulnerable & Outdated Components
- Inventory everything (SBOM — Syft/CycloneDX).
- Automate: Dependabot/Renovate + `trivy`/`osv-scanner` in CI, blocking on CRITICAL.
- An unpatched framework is an open door with a welcome mat.

## A07 — Identification & Authentication Failures
- MFA for anything valuable. Rate-limit + lockout on login.
- Session IDs: regenerate at login, invalidate at logout, `HttpOnly; Secure; SameSite`.
- Delegate when possible: OIDC via Keycloak/Auth0 instead of DIY auth.

## A08 — Software & Data Integrity Failures
- Unsigned updates, compromised CI, malicious dependency versions (supply chain).
- **Fix**: signed commits/artifacts (Cosign), pinned dependencies with lockfiles, protected branches, review on the pipeline itself.

## A09 — Security Logging & Monitoring Failures
- Log auth events, access-control denials, input validation failures — with enough context (who, what, when, from where).
- Never log secrets or full PII. Centralize; alert on patterns (brute force, scraping).

## A10 — Server-Side Request Forgery (SSRF)
Server fetches a URL the attacker controls → reaches internal services / cloud metadata (`169.254.169.254`).
- **Fix**: allowlist of destinations, block private IP ranges, no redirects following, network egress rules.

---

## How to practice (legally)

- [OWASP Juice Shop](https://owasp.org/www-project-juice-shop/) — deliberately vulnerable app to train on
- [PortSwigger Web Security Academy](https://portswigger.net/web-security) — free labs
- OWASP ASVS — the checklist to verify your own apps against
