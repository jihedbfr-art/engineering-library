# X (Twitter)

## Tokens

X v2 wants an OAuth2 user-context token with `tweet.write` and `users.read` for posting text. Create
a project and app in the X developer portal, run the OAuth2 PKCE flow, and store the user token:

```bash
curl -X POST http://localhost:8080/admin/credentials/X \
  -H "Authorization: Bearer $MCP_API_KEY" -H "Content-Type: application/json" \
  -d '{"payload":"<oauth2-user-token>"}'
```

## Config

```yaml
socialpub:
  connectors:
    x:
      enabled: true
```

## The media caveat

Media on X is annoying: uploads still go through the v1.1 chunked `media/upload` endpoint (INIT /
APPEND / FINALIZE) signed with OAuth 1.0a, and only then can you attach `media_ids` to a v2 tweet.
So a full image/video tweet needs *two* credential sets — OAuth2 for the tweet, OAuth1 for the
upload. v1 here posts text tweets and leaves the OAuth1 media path as a roadmap item; the
`XProperties` already carries the four OAuth1 fields for when it's wired.

Rate limits are tight on the free tier — the connector marks `429` responses retryable so the
orchestrator backs off rather than giving up.
