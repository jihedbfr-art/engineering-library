# social-publisher-mcp

One MCP server that publishes to Instagram, Facebook Pages, TikTok, LinkedIn, X and YouTube
Shorts through a single set of tools. Point any MCP client at it — Claude Desktop, Claude Code, a
claude.ai custom connector — and you can cross-post from a conversation without juggling six
different APIs by hand.

It's a Spring Boot app. Platform adapters sit behind one `SocialPublisher` interface, so the MCP
layer never learns anything platform-specific and adding a network is adding a module plus a bean.
Media is staged in MinIO, publications are tracked in Postgres, and the whole thing fans out to the
platforms in parallel with per-platform retry and rate limiting.

## What it does

- `publish_post` — send a caption plus optional media to one or more networks, now or scheduled.
- `list_connected_accounts`, `get_publication_status`, `list_publications`, `cancel_scheduled_post`
- `validate_media` — dry-run a URL against each platform's size/format rules before you commit.
- `suggest_hashtags` — honest keyword extraction, no second LLM hiding behind it.
- `get_platform_limits` — the constraint table as structured data.

There's also a `publication://{id}` resource for the full JSON of a post, and a `cross_post` prompt
that nudges the client to adapt one message per platform before calling `publish_post`.

## 90-second quickstart

You need Docker. Copy the env template and bring the stack up:

```bash
cp .env.example .env      # set MCP_API_KEY and CREDENTIALS_ENC_KEY at least
docker compose -f docker/docker-compose.yml up --build
```

That starts the app on `:8080`, Postgres, and MinIO (with its bucket created for you). Check it:

```bash
curl -s -H "Authorization: Bearer $MCP_API_KEY" http://localhost:8080/mcp
```

Register it with Claude Desktop over HTTP, or run the `stdio` profile locally — see
[docs/claude-desktop.md](docs/claude-desktop.md) for the exact JSON.

To actually post to a network you have to store its token first (the platform apps and their OAuth
dance are on you — that part genuinely can't be automated):

```bash
curl -X POST http://localhost:8080/admin/credentials/LINKEDIN \
  -H "Authorization: Bearer $MCP_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"payload":"<access-token>"}'
```

Each connector only wires itself in when you set `socialpub.connectors.<name>.enabled=true`, so an
un-configured platform simply isn't offered.

## Trying it without real tokens

Run with the `dev` profile and target the `MOCK` platform — the whole pipeline (validation,
staging, persistence, fan-out) runs end to end and returns a fake post URL. That's also what the
end-to-end integration test drives.

## Tool reference

| Tool | What it does |
|---|---|
| `publish_post` | Publish/schedule a caption + media to N platforms; returns a per-platform outcome |
| `list_connected_accounts` | Platforms with stored tokens and their expiry |
| `get_publication_status` | Status, per-platform results and post URLs for one publication |
| `list_publications` | Recent history, filterable by status/platform |
| `cancel_scheduled_post` | Cancel while still `SCHEDULED` |
| `validate_media` | Check a media URL against platform rules without posting |
| `suggest_hashtags` | Keyword-extraction hashtags for a caption |
| `get_platform_limits` | Size/format/duration limits for a platform |

## Per-platform setup

Each network has its own app-creation and token path. The short guides live under
[docs/platforms/](docs/platforms/): [Instagram](docs/platforms/instagram.md),
[Facebook](docs/platforms/facebook.md), [TikTok](docs/platforms/tiktok.md),
[LinkedIn](docs/platforms/linkedin.md), [X](docs/platforms/x.md),
[YouTube](docs/platforms/youtube.md).

## Building

```bash
mvn verify      # unit tests + Testcontainers integration tests (needs Docker)
mvn test        # unit tests only, no Docker
```

Architecture decisions are written up in [docs/adr/](docs/adr/).

## Roadmap

- OAuth for the MCP endpoint itself (v1 uses a static bearer token).
- The X media-upload path (v1.1 chunked + OAuth1); text tweets work today.
- Token refresh jobs for the long-lived tokens (Meta ~60 days).
- Multi-tenant credentials — right now it's single owner, one credential set per platform.

## License

MIT.
