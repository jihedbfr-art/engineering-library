# TikTok

TikTok is last in the build order for a reason: the audit gate. Until your app passes TikTok's
review it can only post privately or as a draft, so `DRAFT` is the default mode here and you should
expect that to be your reality for a while.

## Setup

- A TikTok developer app with the **Content Posting API** product.
- The `video.publish` scope (and `video.upload` for the inbox/draft path).
- An OAuth2 user token with a refresh token.

```bash
curl -X POST http://localhost:8080/admin/credentials/TIKTOK \
  -H "Authorization: Bearer $MCP_API_KEY" -H "Content-Type: application/json" \
  -d '{"payload":"<oauth2-token>"}'
```

## Config

```yaml
socialpub:
  connectors:
    tiktok:
      enabled: true
      mode: DRAFT        # DRAFT sends to the user's inbox; DIRECT publishes (needs an audited app)
      poll-attempts: 15
```

## Flow

The connector uses `PULL_FROM_URL`: it hands TikTok the staged video URL rather than uploading bytes.
`DRAFT` mode initializes at `/v2/post/publish/inbox/video/init/`, `DIRECT` at
`/v2/post/publish/video/init/` with a `post_info` block. Either way you get a `publish_id`, and the
connector polls `/v2/post/publish/status/fetch/` until it reports `SEND_TO_USER_INBOX` (draft) or
`PUBLISH_COMPLETE` (direct).

Because a draft lands in the user's inbox rather than on the profile, there's no permalink to return
— the outcome carries the `publish_id` and you finish the post in the TikTok app.
