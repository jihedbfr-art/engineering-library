# 1. Use Spring AI's MCP server starter instead of the raw MCP SDK

Status: accepted

## Context

The server has to speak the Model Context Protocol over Streamable HTTP (and STDIO for local Claude
Desktop use). There are two realistic ways to do that on the JVM: drive the `io.modelcontextprotocol`
Java SDK directly and hand-roll the transport and tool registration, or use Spring AI's
`spring-ai-starter-mcp-server-webmvc`, which wraps that same SDK and wires it into Spring Boot.

The rest of the app is already Spring Boot — JPA, Flyway, config binding, actuator, scheduling.

## Decision

Use the Spring AI MCP server starter. Tools are plain methods annotated with `@Tool`, collected
into a `ToolCallbackProvider` bean; the resource and prompt are registered as
`SyncResourceSpecification` / `SyncPromptSpecification` beans. The transport, JSON-schema generation
for tool inputs, and the protocol handshake come from the starter.

## Consequences

Far less protocol plumbing to own, and the tools read like ordinary Spring beans, which keeps the
interesting code (orchestration, connectors) front and centre. The cost is a dependency on Spring
AI's abstractions and its release cadence — if the MCP spec moves faster than the starter, we wait.
The underlying SDK is still on the classpath, so dropping to it for something the starter doesn't
expose is possible without ripping anything out. STDIO support is a config profile rather than a
separate build, which is good enough for the local-desktop use case.
