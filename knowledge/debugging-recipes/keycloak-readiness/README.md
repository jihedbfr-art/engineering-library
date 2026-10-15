# Keycloak responds, but its Docker healthcheck fails

[Version française](README.fr.md)

Observed with the MCP Guard demo's `quay.io/keycloak/keycloak:26.0` image: the realm discovery endpoint returned HTTP 200 while `/health/ready` on port 8080 returned 404. The running process and the readiness probe were checking different things.

## Diagnose before restarting

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs --tail 60 keycloak
```

Read the probe's port, path and expected response. Check whether `KC_HEALTH_ENABLED` is enabled. Never infer readiness from an open application port alone. Avoid exporting the entire container environment into a ticket: it may contain credentials.

## Fix verified in this case

Enable `KC_HEALTH_ENABLED=true` and probe the management endpoint inside the container:

```bash
{ printf 'HEAD /health/ready HTTP/1.0\r\n\r\n' >&0; grep 'HTTP/1.0 200'; } 0<>/dev/tcp/localhost/9000
```

This is a Bash command run inside the Keycloak container, not on the host. It needs neither curl in the image nor a published host port 9000. Paths and ports must match the deployed configuration. The [publisher's health guide](https://www.keycloak.org/observability/health) explains the options and probe.

The exact [proposed correction](https://github.com/jihedbfr-art/mcp-keycloak-guard/commit/d05ce06) includes a Compose healthcheck and a bilingual run guide. In a new demo checkout at that commit, wait for readiness with:

```bash
docker compose -f docker/docker-compose.yml up -d --wait --wait-timeout 180
```

## Continue diagnosis above the health layer

| Observation | Next check |
|---|---|
| Container is healthy | Initialize the MCP client using the server's actual transport |
| HTTP request succeeds | Inspect the JSON-RPC envelope and the tool result |
| `result.isError` is true | Diagnose the execution failure; do not report the action as successful |
| A text result is structurally acceptable | Validate its meaning and required permissions before using it |

The [offline response lab](https://github.com/jihedbfr-art/ai-skills/tree/cfdfc41/skills/05-mcp-protocol-and-tools/mcp-sse-transport-and-resource-providers/result-lab) provides six synthetic fixtures. Run `tool-error.json` and `untrusted-content.json`, then explain why neither a transport success nor the classifier's `candidate_success` grants authority. It requires Python only and does not connect to the demo. The pinned snapshot is proposed in a PR, not a release.

## Recorded readiness evidence

On 14 October 2026, the original probe failed with exit code 1. After correction, Compose's wait completed successfully and several consecutive probes returned HTTP 200. The test used an isolated Compose project with no host ports published, then removed that project's container and network.

This demonstrates readiness for the tested demo configuration. It does not test user roles or an MCP tool call. Keep the HTTP health result, authentication result and tool authorization result as separate evidence. The [PR](https://github.com/jihedbfr-art/mcp-keycloak-guard/pull/2) is a proposed change, not a release.

---

<div align="center">
  <img src="../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
