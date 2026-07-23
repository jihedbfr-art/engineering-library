---
name: security-audit-checklist
description: Audit code or a design for common vulnerability classes (injection, broken access control, unsafe deserialization, secrets exposure) using a fixed, defensive checklist. Use when asked to "do a security review", "audit this for vulnerabilities", or before shipping code that handles authentication, user input, or sensitive data. Defensive review only — never generates exploit code or attack instructions.
---

# Security audit checklist

A systematic pass through the vulnerability classes that account for most real-world security
incidents, in the order they're worth checking — this skill is for finding and explaining
defensive gaps, never for producing working exploit code or step-by-step attack instructions.

## Checklist, by class

1. **Injection (SQL, command, LDAP, etc.).** Any place user input reaches a query, shell command,
   or interpreter without parameterization. Look specifically for string concatenation/formatting
   building a query or command from user-controlled input, instead of a parameterized
   query/prepared statement or a safe API.

2. **Broken access control.** Every endpoint/action that should be restricted — is the check
   actually present server-side, not just in the client/UI? Look specifically for authorization
   checks that verify authentication ("is there a valid session") without verifying authorization
   ("is *this* user allowed to act on *this* resource") — the classic gap that lets one
   authenticated user access another's data by changing an ID in a request.

3. **Sensitive data exposure.** Secrets, credentials, or tokens in code, logs, error messages, or
   version control. Look specifically for a stack trace or error response that leaks internal
   details (file paths, query text, stack frames) to an end user rather than a generic message
   with details logged server-side only.

4. **Unsafe deserialization.** Any deserialization of untrusted input using a format/library
   capable of instantiating arbitrary types or executing code as a side effect of parsing —
   flag it even without demonstrating exploitation, the capability itself is the risk.

5. **Weak or missing input validation.** Not "does it validate," but "does it validate on the
   server, not just trust client-side validation" — client-side checks are a UX convenience, not
   a security boundary; anything reachable by a direct API call bypasses them entirely.

6. **Cryptography misuse.** Passwords hashed with a fast general-purpose hash instead of a
   password-specific one (bcrypt/argon2/scrypt), a hardcoded or weak encryption key, or
   home-rolled crypto instead of a vetted library/standard primitive.

7. **Dependency and supply-chain exposure.** Are dependencies pinned and from a known source? A
   security audit of application code that ignores a critically vulnerable pinned dependency
   version is incomplete.

## How to report findings

For each finding: which class from the list above, the specific location, the concrete scenario
that would trigger it (not just "this could be exploited" — describe the actual attacker
capability this gap grants), and the defensive fix. Never include a working payload/exploit —
describe the vulnerability class and impact, that's sufficient for the fix to be understood and
applied.

## What NOT to do

- Never generate working exploit code, attack scripts, or step-by-step instructions for
  compromising a system — describe *why* something is vulnerable and *how to fix it*, not how to
  exploit it.
- Don't flag theoretical risks with no plausible attack path as equally urgent to a directly
  exploitable one — prioritize by real reachability, not by category alone.
- Don't skip low-glamour findings (a verbose error message, a missing rate limit) in favor of
  more "interesting" ones — the checklist order above reflects real-world incident frequency, not
  technical novelty.
