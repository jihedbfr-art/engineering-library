# Facebook Pages

Publishing goes to a Page, never a personal profile, so everything hangs off a Page access token.

## Getting a Page token

1. Create a Meta app (developers.facebook.com) and add the **Facebook Login** and **Pages** access.
2. Grant `pages_manage_posts`, `pages_read_engagement`, and `pages_show_list`.
3. Exchange a user token for a Page token via `GET /me/accounts`. That Page token is what you store.

```bash
curl -X POST http://localhost:8080/admin/credentials/FACEBOOK \
  -H "Authorization: Bearer $MCP_API_KEY" -H "Content-Type: application/json" \
  -d '{"payload":"<page-access-token>"}'
```

## Config

```yaml
socialpub:
  connectors:
    facebook:
      enabled: true
      page-id: "1234567890"
```

## How posts are routed

- text only → `/{page-id}/feed`
- image → `/{page-id}/photos` with `url=`
- video → `/{page-id}/videos` with `file_url=`

The Graph API accepts the Page token as a bearer header, which keeps the connector from having to
smuggle it into the query string. The `post_id` from the response is preferred over the bare `id`
because that's the one that maps to a viewable post.
