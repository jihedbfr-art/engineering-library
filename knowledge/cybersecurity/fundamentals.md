# Security Fundamentals

## The CIA triad

- **Confidentiality** — only authorized eyes see the data (encryption, access control)
- **Integrity** — data isn't tampered with (hashes, signatures, immutability)
- **Availability** — the system stays up (redundancy, rate limiting, backups)

Every security control protects at least one of these. Every attack targets at least one.

## Core vocabulary

| Term | Meaning |
|---|---|
| Vulnerability | A weakness (e.g. unpatched library) |
| Exploit | Code/technique that uses the weakness |
| Threat | Someone/something that might use it |
| Risk | Likelihood × impact — what you actually manage |
| Attack surface | Everything exposed: ports, endpoints, dependencies, people |
| Zero-day | Vulnerability unknown to the vendor (no patch exists yet) |
| CVE | Public identifier of a known vulnerability (CVE-2024-XXXX) |
| CVSS | Severity score 0–10 of a CVE |

## Threat modeling — STRIDE

Before writing code, ask what can go wrong:

- **S**poofing — pretending to be someone else → strong auth
- **T**ampering — modifying data/code → signatures, integrity checks
- **R**epudiation — denying an action → audit logs
- **I**nformation disclosure — leaks → encryption, access control
- **D**enial of service — making it unavailable → rate limits, autoscaling
- **E**levation of privilege — becoming admin → least privilege, sandboxing

## Cryptography survival kit (use, don't invent)

| Need | Use | Never |
|---|---|---|
| Password storage | argon2id, bcrypt | MD5, SHA-1, plain SHA-256 |
| Data in transit | TLS 1.2+ | Custom protocols |
| Data at rest | AES-256-GCM | ECB mode, homemade XOR |
| Integrity/signature | HMAC-SHA-256, Ed25519 | Truncated hashes |
| Randomness | CSPRNG (`SecureRandom`, `crypto.randomBytes`) | `Math.random()` for anything secret |

Two asymmetric key facts everyone should know:
1. Public key encrypts / verifies. Private key decrypts / signs. Private keys never travel.
2. TLS uses asymmetric crypto only to establish a symmetric session key (speed).

## Defense in depth

Assume every single layer will fail. Stack them:

```
Edge (WAF, DDoS protection)
→ Network (segmentation, firewall, zero trust)
→ Host (hardening, patching, EDR)
→ Application (secure code, authz on every request)
→ Data (encryption, minimal retention)
→ Monitoring (detect what got through anyway)
```

## Least privilege — the most profitable habit

Every account, token, service and container gets the minimum access needed, for the minimum time needed. Most breach write-ups are a chain of over-privileged things.

If I had to pick one habit that prevents the most damage per hour invested, it's this one, not encryption, not fancy detection tooling. Encryption protects data at rest; least privilege limits what an attacker can even reach once they're in — and they will eventually get in, somewhere, somehow. I'd rather an attacker land on a container that can only read one S3 bucket than one with a role that can touch the whole account. That's the actual difference between an incident and a headline.
