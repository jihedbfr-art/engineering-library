# Contributing

## Adding a new platform in five steps

The whole point of the module layout is that a new network doesn't touch the core or the MCP layer.
Say you're adding Threads:

1. **New module.** Copy one of the `connector-*` modules (LinkedIn is the simplest). Rename the
   artifact to `connector-threads` and add it to the parent `pom.xml` `<modules>` and to `app`'s
   dependencies.
2. **Add the enum value.** `Platform.THREADS` in `connector-api`. This is the only shared file you
   touch.
3. **Implement `SocialPublisher`.** One class, one `publish(PublishCommand)` method. Read the token
   from the injected `CredentialProvider`, call the API with a `RestClient`, map the response to
   `PlatformOutcome.ok(...)` or `PlatformOutcome.fail(...)`. Return failures with `retryable=true`
   for transient problems so the orchestrator's retry does its job.
4. **Wire it conditionally.** A `@Configuration` with `@EnableConfigurationProperties` and
   `@ConditionalOnProperty(name = "socialpub.connectors.threads.enabled", havingValue = "true")` so
   the connector only registers when it's configured. Bean discovery does the rest — the
   orchestrator picks it up from the `List<SocialPublisher>`.
5. **Add limits and a test.** Put the media rules in `PlatformConstraints` if the platform is
   media-heavy, and write a `MockWebServer` test replaying a real response payload. Fixtures go under
   `src/test/resources/fixtures/`. A `@Disabled("requires live credentials")` integration test that
   hits the real API is welcome too.

That's it — no changes to `PublicationService`, `PublishingTools`, or any MCP wiring.

## Before you push

- `mvn spotless:apply` (google-java-format) and `mvn verify`.
- Keep `core` free of Spring Web — the enforcer rule will fail the build if a vendor SDK or
  `spring-web` sneaks in.
- One commit per logical unit reads better than one big drop.
