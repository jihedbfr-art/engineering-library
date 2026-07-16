# REST API Design — Practical Rules

## Resources & verbs

```
GET    /notes            list (filter/sort/paginate via query params)
POST   /notes            create → 201 + Location header
GET    /notes/42         read one → 200 or 404
PUT    /notes/42         full replace (idempotent)
PATCH  /notes/42         partial update
DELETE /notes/42         delete → 204 (idempotent: repeat = still 204/404)
```

- Nouns, plural, no verbs in paths (`/notes/42/archive` → prefer `PATCH {status: archived}`; a POST action-subresource is OK when it's genuinely an action).
- Nesting max one level: `/users/7/notes` fine, `/users/7/notes/42/tags/3/...` no — flatten with filters.

## Status codes that matter

| Code | Use |
|---|---|
| 200/201/204 | OK / created / done-nothing-to-return |
| 400 | Malformed request (validation) |
| 401 | Who are you? (no/invalid token) |
| 403 | I know you, you can't do this |
| 404 | Not found — also for "exists but you can't know that" |
| 409 | Conflict (duplicate, version clash) |
| 422 | Valid JSON, invalid business-wise (some teams fold into 400 — pick one, document it) |
| 429 | Rate limited (+ Retry-After) |
| 500 | Your bug. Log it, alert on it, never leak the stack trace |

## Error body — one consistent shape

```json
{
  "type": "https://api.example.com/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "title must not be empty",
  "instance": "/notes",
  "errors": [{ "field": "title", "message": "must not be empty" }]
}
```
(RFC 9457 Problem Details — free standard, use it.)

## Pagination — do it from day one

```
GET /notes?page=0&size=20&sort=createdAt,desc         (offset — simple, fine to start)
GET /notes?cursor=eyJpZCI6NDJ9&size=20                (cursor — stable under inserts, scales)
```
Response includes paging metadata (`totalElements` or `nextCursor`). An unpaginated list endpoint is a future outage.

## Versioning

- Path versioning (`/api/v1/...`) is the pragmatic default.
- Additive changes (new optional fields) don't need a version bump — clients must ignore unknown fields.
- Breaking = removal, rename, type change, semantics change. Announce, dual-run, sunset.

## Security baseline

- AuthN via OIDC bearer tokens; **authorization checked per resource** (owner/role), not just "is logged in" — see [OWASP A01](../../devsecops/security/owasp-top10.md).
- Validate input server-side (size limits included). Rate limit per client.
- Don't leak: internal ids where guessable ids hurt, stack traces, framework banners.

## Idempotency for the real world

Clients retry (timeouts, flaky mobile). Make it safe:
- PUT/DELETE naturally idempotent — keep them so.
- For POST payments/orders: accept an `Idempotency-Key` header; same key → same result, no duplicate.

## When REST isn't the right call

Internal service-to-service traffic at scale, or a client that needs to shape its own response — see [gRPC and GraphQL](grpc-and-graphql.md) for when to reach past REST and what it actually costs you.
