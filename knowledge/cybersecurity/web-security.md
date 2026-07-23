# Web Security — Attacks & Defenses

Understanding how attacks work is how you build defenses that hold. Practice only on legal labs (Juice Shop, PortSwigger Academy).

## XSS — Cross-Site Scripting

Attacker's JavaScript runs in the victim's browser (session theft, keylogging, defacement).

- **Reflected**: payload in the URL, reflected by the server.
- **Stored**: payload saved (comment, profile) and served to every visitor. Worst kind.
- **DOM-based**: front-end JS injects untrusted data into the DOM.

**Defense:**
1. Output encoding by context (HTML, attribute, JS, URL) — frameworks like Angular/React do this by default; danger appears with `innerHTML`, `bypassSecurityTrust*`, `dangerouslySetInnerHTML`.
2. `Content-Security-Policy` header — kills most injected scripts.
3. `HttpOnly` cookies — stolen DOM can't read the session.

## CSRF — Cross-Site Request Forgery

Victim's browser sends an authenticated request the victim never intended (their cookies ride along).

**Defense:** SameSite cookies (`Lax`/`Strict`), anti-CSRF tokens for state-changing requests, never GET for mutations.

## SQL Injection

Covered in depth in [OWASP A03](../devsecops/security/owasp-top10.md). One line: **parameterized queries, always, no exceptions.**

## Authentication attacks

| Attack | Defense |
|---|---|
| Credential stuffing (leaked pw reuse) | MFA, breach-password blocklists (haveibeenpwned API) |
| Brute force | Rate limit + progressive delays + lockout |
| Session fixation | Regenerate session ID at login |
| JWT tampering | Verify signature server-side, reject `alg: none`, short expiry, validate `aud`/`iss` |

### JWT specifics (common in microservices)

- The payload is **base64, readable by anyone** — never put secrets in it.
- Signature verification is what makes it trustworthy — every service must verify, not just the gateway.
- Prefer short-lived access tokens + refresh token rotation.

## Security headers checklist

```
Content-Security-Policy: default-src 'self'
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY            (or CSP frame-ancestors)
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: camera=(), microphone=(), geolocation=()
```
Test yours: securityheaders.com

## CORS — often misunderstood

- CORS is the browser *relaxing* the Same-Origin Policy, not a security feature you add.
- `Access-Control-Allow-Origin: *` + credentials = data leak.
- Reflecting the request's `Origin` header without an allowlist = same as `*`.
- CORS does **not** protect your API from non-browser clients — authz does.

## File upload dangers

1. Validate type by content (magic bytes), not extension.
2. Re-encode images; store outside the web root, serve via a handler.
3. Random storage names; size limits; scan if high risk.
4. Never let the client choose the path (`../../etc/cron.d/...` — path traversal).
