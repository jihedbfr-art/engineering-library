# HTTP — The Protocol of the Web

## Methods (verbs)

| Method | Purpose | Safe? | Idempotent? |
|---|---|---|---|
| GET | read | ✅ | ✅ |
| POST | create / action | ❌ | ❌ |
| PUT | replace | ❌ | ✅ |
| PATCH | partial update | ❌ | ❌ |
| DELETE | remove | ❌ | ✅ |
| HEAD | headers only | ✅ | ✅ |
| OPTIONS | capabilities (CORS preflight) | ✅ | ✅ |

*Safe* = no state change. *Idempotent* = repeating it has the same effect as doing it once (crucial for safe retries).

## Status codes

```
1xx  informational
2xx  success        200 OK · 201 Created · 204 No Content
3xx  redirect       301 Moved Permanently · 304 Not Modified
4xx  client error   400 Bad Request · 401 Unauthorized · 403 Forbidden
                    404 Not Found · 409 Conflict · 429 Too Many Requests
5xx  server error   500 Internal · 502 Bad Gateway · 503 Unavailable · 504 Timeout
```
Memory hook: **4xx = you messed up, 5xx = server messed up.** 401 = "who are you?", 403 = "I know you, no."

## Headers that matter

```
# Request
Host: api.example.com
Authorization: Bearer <token>
Content-Type: application/json
Accept: application/json
Cookie: session=abc

# Response
Content-Type: application/json; charset=utf-8
Cache-Control: max-age=3600, public
Set-Cookie: session=abc; HttpOnly; Secure; SameSite=Lax
ETag: "a1b2c3"                 # for conditional requests / caching
Location: /notes/42            # after 201/redirect
```

## Statelessness & how state is faked

HTTP itself is **stateless** — each request is independent. "Logged in" state is carried by:
- **Cookies** (session id the server looks up, or a signed token)
- **Authorization header** (bearer tokens / JWT) → common for APIs

## Caching (huge for performance)

- `Cache-Control: max-age=N` — cache for N seconds.
- `ETag` + `If-None-Match` — server replies **304 Not Modified** (no body) if unchanged → saves bandwidth.
- CDNs cache at the edge, near users.

## HTTP versions

- **HTTP/1.1** — text, one request at a time per connection (head-of-line blocking).
- **HTTP/2** — binary, multiplexed (many requests over one connection), header compression.
- **HTTP/3** — over QUIC (UDP-based), faster connection setup, better on flaky networks.

## HTTPS = HTTP + TLS

Encrypts everything in transit, authenticates the server, prevents tampering. Non-negotiable today — see [web security](../cybersecurity/web-security.md) for the security headers to add on top.
