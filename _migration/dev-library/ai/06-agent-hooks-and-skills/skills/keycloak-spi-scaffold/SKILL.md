---
name: keycloak-spi-scaffold
description: Scaffold a new Keycloak Service Provider Interface (SPI) module — Authenticator, Event Listener, or User Storage Provider — with the Provider/ProviderFactory pair, the META-INF/services registration file Keycloak actually needs to discover it, and a starting unit test. Use when asked to "create a Keycloak SPI", "add a custom authenticator to Keycloak", or "write a Keycloak provider" for authentication flows, event forwarding, or federating an external user store.
---

# Keycloak SPI scaffold

Every Keycloak SPI module follows the same three-piece shape regardless of which SPI it
implements: a `Provider` (the per-request instance doing the work), a `ProviderFactory` (creates
provider instances, holds shared config), and a `META-INF/services/<FactoryInterface>` file
listing the factory's fully-qualified class name — **without this file, Keycloak never discovers
the provider at all**, silently. This is the single most common reason a freshly-written SPI
"does nothing": the classes compile and the jar deploys, but Keycloak's `ServiceLoader` never
finds it because the services file is missing, misnamed, or lists the wrong class.

## Which SPI to scaffold

Ask which of these three the task actually needs before generating anything — they have
different base interfaces and different registration points:

| SPI type | Provider interface | Factory interface | Registers in Keycloak as |
|---|---|---|---|
| Authenticator | `Authenticator` (or `ConditionalAuthenticator` for a gated step) | `AuthenticatorFactory` | A step in an authentication flow |
| Event listener | `EventListenerProvider` | `EventListenerProviderFactory` | A listener forwarding LOGIN/LOGOUT/etc. events |
| User storage | `UserStorageProvider` + `UserLookupProvider` (+ `UserQueryMethodsProvider` for search) | `UserStorageProviderFactory` | A federated read-only or read-write user source |

## Scaffold checklist, in order

1. **Provider class** implementing the interface(s) from the table above. Keep it focused on one
   responsibility — an Authenticator that also does event forwarding is two SPIs wearing one
   trenchcoat, split it instead.
2. **ProviderFactory class** implementing `create(KeycloakSession)` (returns a new provider
   instance per request/session — providers are not meant to be shared/stateful across requests)
   and `getId()` (a short, unique string — this is the identifier that shows up in the Keycloak
   admin console and in any config referencing this provider by ID).
3. **`META-INF/services/<fully-qualified-factory-interface>`** — a plain text file, one line:
   the fully-qualified name of your factory class. This is not optional and not inferred from
   annotations; verify it exists and matches before considering the scaffold complete.
4. **A unit test** for the provider's core logic using Mockito to mock `KeycloakSession` and
   whatever the provider depends on — the provider logic itself should be testable without
   needing a running Keycloak instance. A real integration test against a live Keycloak
   (Testcontainers, `quay.io/keycloak/keycloak`) is a separate, heavier concern — mention it as a
   next step, don't scaffold it by default unless asked.
5. **A one-paragraph README note** on what config, if any, the factory reads (via
   `AuthenticatorConfigModel` for authenticators, or provider-specific config for others) — SPI
   config is easy to wire wrong and silently fall back to defaults, worth documenting explicitly.

## Known traps to avoid when generating the scaffold

- Don't generate a User Storage Provider's credential handling using
  `LegacyUserCredentialManager` or similar older-tutorial class names — recent Keycloak versions
  require implementing `SubjectCredentialManager` directly, bridging back to
  `CredentialInputValidator`. Always check the actual interface shape in the target Keycloak
  version rather than assuming an older tutorial's class names still exist.
- `UserStorageProvider` pulling from JDBC directly: don't scaffold a connection pool inline —
  leave a clearly marked seam (a constructor parameter or injected `DataSource`) and note that
  production use needs one, rather than silently shipping an unpooled `DriverManager.getConnection`
  call that looks production-ready but isn't.
- For a `ConditionalAuthenticator`, remember `matchCondition()` decides whether the step runs at
  all, and `authenticate()` decides pass/fail — don't collapse both into one method, the flow
  engine calls them at different points.

## Output format

Produce the actual files (Provider, ProviderFactory, the services file, the test) rather than a
description of what they should contain — this is a scaffold skill, its job is working starting
code, not an explanation of the SPI system.
