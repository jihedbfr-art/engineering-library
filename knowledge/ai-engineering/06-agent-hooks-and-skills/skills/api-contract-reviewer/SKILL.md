---
name: api-contract-reviewer
description: Review a REST (or GraphQL/gRPC) API design or diff for contract-breaking changes, inconsistent error shapes, wrong HTTP semantics, and missing versioning/pagination. Use when reviewing an API design doc, an OpenAPI spec, or a diff that adds/changes endpoints, or when asked to "review this API" or "check for breaking changes".
---

# API contract review

An API's real cost isn't the endpoint that's wrong today — it's the client that broke silently
because a "safe-looking" change wasn't. Review for contract stability first, style second.

## What to check, in priority order

1. **Breaking changes disguised as safe ones.** Removing a field, renaming a field, changing a
   field's type, or changing an enum's allowed values are breaking even if "nobody probably uses
   it." Adding a *required* field to a request body is breaking for existing clients; adding an
   *optional* one is not. Flag every field-level change against this distinction explicitly.

2. **HTTP method semantics.** `GET` must be safe (no side effects) and idempotent. `PUT` must be
   idempotent (calling it twice with the same body produces the same end state). `POST` is
   neither by default — if an endpoint needs `POST` to be safely retryable, it needs an explicit
   idempotency key, not an assumption that retries won't happen. `DELETE` on an already-deleted
   resource returning an error instead of a clean success is a common idempotency violation worth
   flagging.

3. **Consistent error shape across every endpoint.** One endpoint returning
   `{"error": "message"}` and another returning `{"errors": [{"code": ..., "detail": ...}]}` in
   the same API forces every client to special-case error handling per endpoint. Flag any new
   endpoint whose error response shape doesn't match the rest of the API — RFC 7807 (Problem
   Details) is a reasonable default if there's no existing convention to match.

4. **Pagination on any endpoint that can return an unbounded list.** A collection endpoint with
   no pagination today is a production incident waiting for the data volume to grow — flag any
   list endpoint without cursor or offset pagination, and flag pagination whose response doesn't
   indicate whether more pages exist.

5. **Versioning strategy present and consistent.** Whatever the chosen approach (URL path,
   header, content negotiation), a new endpoint that doesn't follow the same versioning
   convention as the rest of the API creates a hidden inconsistency clients will hit eventually.
   Flag a new endpoint that doesn't specify a version if the rest of the API does.

6. **Auth and rate-limit behavior specified**, not assumed — a spec that doesn't say what an
   unauthenticated or over-limit request returns leaves every client guessing, and every
   implementation guessing differently.

## How to report findings

For each issue: is it a **breaking change** (blocks merge until versioned or reworked), an
**inconsistency** (should fix, not necessarily blocking), or a **missing safeguard** (pagination,
rate limits — flag before it becomes a real incident, not after). Lead with breaking changes.

## What NOT to flag

- Internal/private APIs explicitly marked as having no external consumers and no stability
  guarantee — the contract-stability rules above matter most where clients you don't control
  depend on the contract.
- Style choices (field naming convention, snake_case vs camelCase) unless they contradict an
  existing, established convention in the same API.
