# Instagram

Instagram is the same Meta Graph API as Facebook, but the publish flow is three calls instead of
one, and it only accepts media — no text-only posts.

## Prerequisites

- An Instagram **professional** account (Business or Creator).
- It must be linked to a Facebook Page.
- A Meta app with `instagram_basic`, `instagram_content_publish`, and the Page permissions.

You post with the linked account's IG user id and a token that carries the publish scope.

## Config

```yaml
socialpub:
  connectors:
    instagram:
      enabled: true
      ig-user-id: "1789..."
      poll-attempts: 15
```

```bash
curl -X POST http://localhost:8080/admin/credentials/INSTAGRAM \
  -H "Authorization: Bearer $MCP_API_KEY" -H "Content-Type: application/json" \
  -d '{"payload":"<token>","expiresAt":"2026-09-01T00:00:00Z"}'
```

## The three-step dance

1. `POST /{ig-user-id}/media` with `image_url` (or `video_url` + `media_type=REELS`) and the caption
   — this creates a *container*.
2. Poll `GET /{container-id}?fields=status_code` until it reads `FINISHED`. Video containers take a
   while; that's what `poll-attempts` is for.
3. `POST /{ig-user-id}/media_publish` with the container id.

Then a follow-up read fetches the `permalink`. Meta tokens are long-lived (~60 days) — a refresh job
before expiry is on the roadmap, so for now watch the `expiresAt` you stored.
