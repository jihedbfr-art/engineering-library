# Registering with Claude Desktop

Two ways to connect, depending on whether you run the server as an HTTP service or as a local
process.

## HTTP (the compose stack)

If you brought the server up with `docker compose`, it's listening on `http://localhost:8080/mcp`
behind the bearer token you set as `MCP_API_KEY`. Add it to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "social-publisher": {
      "url": "http://localhost:8080/mcp",
      "headers": {
        "Authorization": "Bearer YOUR_MCP_API_KEY"
      }
    }
  }
}
```

From Claude Code the equivalent is:

```bash
claude mcp add --transport http social-publisher http://localhost:8080/mcp \
  --header "Authorization: Bearer YOUR_MCP_API_KEY"
```

## STDIO (local jar, no web server)

For a purely local setup, run the packaged jar under the `stdio` profile. There's no HTTP server and
no bearer token — the transport is the process's stdin/stdout, so keep logs off the console (the
profile already routes them to a file).

```json
{
  "mcpServers": {
    "social-publisher": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/social-publisher-mcp.jar",
        "--spring.profiles.active=stdio"
      ],
      "env": {
        "DB_URL": "jdbc:postgresql://localhost:5432/socialpub",
        "DB_USER": "socialpub",
        "DB_PASSWORD": "socialpub",
        "MINIO_ENDPOINT": "http://localhost:9000",
        "MINIO_ACCESS_KEY": "minioadmin",
        "MINIO_SECRET_KEY": "minioadmin",
        "CREDENTIALS_ENC_KEY": "your-base64-32-byte-key"
      }
    }
  }
}
```

The STDIO process still needs Postgres and MinIO reachable, so you'll typically run those two from
the compose file and only the app locally.

Once it's registered, restart Claude Desktop and the eight tools plus the `cross_post` prompt show
up. A good first message: *"validate this image for Instagram and X, then draft a cross-post."*
