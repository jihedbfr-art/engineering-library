# YouTube Shorts

Uploads use the Data API v3 resumable protocol and a Google OAuth2 token.

## Setup

1. In Google Cloud, enable the **YouTube Data API v3** and create OAuth client credentials.
2. Run the consent flow for the `https://www.googleapis.com/auth/youtube.upload` scope.
3. Store the access token (with its refresh token if you want the future refresh job to use it):

```bash
curl -X POST http://localhost:8080/admin/credentials/YOUTUBE \
  -H "Authorization: Bearer $MCP_API_KEY" -H "Content-Type: application/json" \
  -d '{"payload":"<access-token>"}'
```

## Config

```yaml
socialpub:
  connectors:
    youtube:
      enabled: true
      shorts-mode: true          # appends #Shorts to the title
      privacy-status: unlisted   # public | unlisted | private
```

## How the upload works

Two steps. First a `POST /upload/youtube/v3/videos?uploadType=resumable&part=snippet,status` with the
title/description/privacy JSON — the response comes back with a `Location` header, the session URL.
Then the connector downloads the staged video and PUTs the bytes to that session URL. The video id
in the final response becomes `https://youtube.com/shorts/{id}`.

Title defaults to the first line of the caption (or the per-platform `title` override) with
`#Shorts` tacked on when shorts mode is set. Default privacy is `unlisted` on purpose — nothing goes
public without you asking.
