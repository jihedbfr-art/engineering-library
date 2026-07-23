# HTTP Cheatsheet

## Status codes — the ones that actually come up

```
2xx Success
  200 OK                      standard success
  201 Created                 + Location header pointing to the new resource
  204 No Content              success, nothing to return (common for DELETE)

3xx Redirection
  301 Moved Permanently       update your bookmarks/links
  302 Found                   temporary redirect
  304 Not Modified            cached version is still valid (conditional GET)

4xx Client error
  400 Bad Request             malformed request
  401 Unauthorized            no/invalid credentials — you don't know who I am
  403 Forbidden               I know who you are, you can't do this
  404 Not Found
  405 Method Not Allowed      wrong verb for this endpoint
  409 Conflict                state conflict (duplicate, version mismatch)
  422 Unprocessable Entity    well-formed but semantically invalid
  429 Too Many Requests       rate limited — check Retry-After

5xx Server error
  500 Internal Server Error   generic, your bug
  502 Bad Gateway             upstream server sent an invalid response
  503 Service Unavailable     overloaded/down — often temporary
  504 Gateway Timeout         upstream took too long
```
401 vs 403, explained once and for all: **401 = "I don't know who you are"** (missing/invalid auth), **403 = "I know exactly who you are, and no"** (authenticated but not authorized).

## Methods

```
GET      read, no side effects, cacheable, safe to retry
POST     create / non-idempotent action
PUT      full replace, idempotent (same request twice = same result)
PATCH    partial update
DELETE   remove, idempotent
HEAD     like GET but headers only, no body
OPTIONS  what does this endpoint support (also the CORS preflight method)
```

## Headers worth actually knowing

```
Content-Type: application/json           # what the body IS
Accept: application/json                 # what you WANT back
Authorization: Bearer <token>            # auth credentials
Cache-Control: no-cache, max-age=3600    # caching directives
ETag: "abc123"                           # a version fingerprint for the resource
If-None-Match: "abc123"                  # conditional GET — 304 if unchanged
Idempotency-Key: <uuid>                  # client-supplied, prevents duplicate side effects on retry
```

## Caching, briefly (the part everyone half-remembers)

```
Cache-Control: no-store              never cache
Cache-Control: no-cache              cache it, but revalidate before using
Cache-Control: max-age=3600          cache for 1 hour, no revalidation needed
Cache-Control: private               only the browser can cache (not a shared CDN/proxy)
Cache-Control: public                anyone (including CDNs) can cache
```

## curl — the essential flags

```bash
curl -X POST https://api.example.com/notes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title": "Hello"}'

curl -i https://api.example.com/notes/1        # -i: include response headers
curl -s -o /dev/null -w "%{http_code} %{time_total}s\n" URL   # status + timing only
curl -L https://short.link                      # -L: follow redirects
curl -v https://api.example.com                  # -v: verbose, full handshake
```

## TLS handshake, the short version

```
Client → Server:  "here's what I support" (ClientHello)
Server → Client:  certificate + chosen cipher
Client verifies certificate against trusted CAs
Both sides derive a shared symmetric session key
        → encrypted communication begins
```
Asymmetric crypto is only used to safely exchange a symmetric key — everything after the handshake uses fast symmetric encryption (AES). See [crypto basics](../../cybersecurity/fundamentals.md).

## HTTP/1.1 vs HTTP/2 vs HTTP/3, in one line each

- **HTTP/1.1**: text-based, one request per connection at a time (without pipelining hacks).
- **HTTP/2**: binary framing, multiplexed (many requests over one connection), header compression.
- **HTTP/3**: runs over QUIC (UDP-based) instead of TCP — avoids TCP head-of-line blocking, faster connection setup, better on lossy mobile networks.

## The idempotency table (comes up in API design constantly)

| Method | Idempotent? | Safe (no side effects)? |
|---|---|---|
| GET | ✅ | ✅ |
| PUT | ✅ | ❌ |
| DELETE | ✅ | ❌ |
| PATCH | ⚠️ depends on implementation | ❌ |
| POST | ❌ | ❌ |

See [REST API design](../../backend/apis/rest-api-design.md) for how this shapes real endpoint design.
