# LinkedIn

LinkedIn was the least painful of the six to get working, which is why the execution order starts
here.

## What you need

- A LinkedIn app (developer.linkedin.com) with the **Share on LinkedIn** and **Sign In with
  LinkedIn using OpenID Connect** products added.
- The `w_member_social` scope for posting.
- Your author id. For a person that's the `sub` you get back from the userinfo endpoint, wrapped by
  the connector into `urn:li:person:{id}`. For a company page use the organization id and set the
  author type to `ORGANIZATION`.

## Config

```yaml
socialpub:
  connectors:
    linkedin:
      enabled: true
      author-type: PERSON        # or ORGANIZATION
      author-id: "abc123"
      version: "202401"          # LinkedIn-Version header; bump as the API versions
```

Store the access token:

```bash
curl -X POST http://localhost:8080/admin/credentials/LINKEDIN \
  -H "Authorization: Bearer $MCP_API_KEY" -H "Content-Type: application/json" \
  -d '{"payload":"<access-token>","expiresAt":"2026-09-01T00:00:00Z"}'
```

## Notes

v1 posts the caption as a text share and reads the created URN back from the `x-restli-id` response
header. Image and video posts follow the same Posts API with an extra upload-initialize step — the
request shapes are in the connector, wiring the binary upload is a TODO.
