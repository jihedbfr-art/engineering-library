# MCP tool call loses the authenticated user

> The SSE connection accepts a bearer token, but a protected tool reports an authentication error. Even an administrator cannot call it.

This recipe applies to a Spring Boot 3.3.5, Spring AI 1.0.0 and Spring Security resource-server setup using the WebMVC MCP server. Other transports and versions need their own checks.

## Likely causes

1. The request is rejected before the MCP session starts: missing or invalid token, issuer mismatch, or a security rule on `/sse`.
2. The token is accepted, but its realm roles are not mapped to the authorities used by `@PreAuthorize`.
3. The HTTP request has an authenticated `SecurityContext`, but the tool callback runs on a Reactor worker without that thread-local context.

## Diagnose in order

1. Call `GET /sse` without a token and with a valid test token. A `401` for the first request only establishes the HTTP boundary; it says nothing about tool authorization.
2. With a real MCP client, call one read-only tool as a reader and one privileged tool as an administrator. Record the MCP result, including its error flag. A tool denial may be returned as an MCP error rather than an HTTP `403`.
3. Compare those results with a direct call to the same `@PreAuthorize` method under a test authentication. If the direct call succeeds and the MCP call fails for the same administrator, inspect the asynchronous context boundary before changing roles.
4. Repeat the privileged call as a reader after the administrator call, then run both clients concurrently. The reader must remain denied in both cases. Never print bearer tokens in logs or test output.

In a Maven project with an integration test that starts its identity provider and uses an MCP client, run:

```bash
mvn -q verify
```

Inspect the test report and assert all four observations: anonymous SSE rejected, reader tool allowed, reader privileged tool denied, administrator privileged tool allowed. The direct method test alone cannot establish this behavior.

## Fix for the confirmed context-loss case

The tested reference server registers a Micrometer `ThreadLocalAccessor` for `SecurityContextHolder` and enables Reactor automatic context propagation before tool callbacks run. Its protected methods keep `@PreAuthorize`; the tool still checks the current caller's roles. Test the whole MCP route again after the change, especially two simultaneous clients with different roles.

The relevant configuration in that Spring AI 1.0.0 setup is:

```java
@PostConstruct
void enableSecurityContextPropagation() {
  ContextRegistry.getInstance()
      .registerThreadLocalAccessor(
          "spring.security.context",
          SecurityContextHolder::getContext,
          SecurityContextHolder::setContext,
          SecurityContextHolder::clearContext);
  Hooks.enableAutomaticContextPropagation();
}
```

Register the accessor and hook at startup, before new Reactor subscriptions are made. Keep the MCP client integration test: this configuration snippet by itself is not proof that the caller's context reaches the callback in another application.

Do not copy a security context into a global variable or infer identity from a tool argument. The context belongs to the authenticated request. If the token or role mapping is wrong, repair that boundary instead of adding propagation.

## If it still fails

Check the actual MCP transport paths and the Spring AI version in use. In the reference setup, the SSE endpoint is `/sse` and messages use `/mcp/message`; a POST to `/mcp` returns `404`. A passing five-test integration suite in that setup covered sequential and one simultaneous reader/admin pair. It was not a load test.

Reference implementation: [mcp-keycloak-guard](https://github.com/jihedbfr-art/mcp-keycloak-guard). The diagnosis above comes from its local integration tests; verify the relevant commits are published before treating the linked default branch as an implementation example.

Official references: [Spring AI 1.0 MCP server transport](https://docs.spring.io/spring-ai/reference/1.0/api/mcp/mcp-server-boot-starter-docs.html) and [Reactor context propagation](https://projectreactor.io/docs/core/release/reference/advanced-contextPropagation.html).
