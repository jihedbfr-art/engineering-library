# gRPC and GraphQL — when REST isn't the right default

REST is the safe default for [public and partner APIs](rest-api-design.md). It stops being the right choice in two specific situations: service-to-service calls inside a microservices platform where latency and contract strictness matter more than browsability, and client-facing APIs where the client needs to shape the response instead of taking whatever the resource endpoint hands back. gRPC solves the first. GraphQL solves the second. Neither replaces REST wholesale — picking one for everything is how teams end up maintaining three ways to do the same thing.

## gRPC — for internal service-to-service traffic

If you're running a Spring Cloud / Spring Boot microservices platform (see [spring-microservices.md](../microservices/spring-microservices.md)) and your services call each other over HTTP+JSON today, gRPC is usually the first infra upgrade worth the migration cost — but only past a certain service count. Below ~5-6 services, REST-over-HTTP internally is fine and gRPC adds tooling overhead for no real gain.

**What actually changes vs REST:**

- **Contract-first, not code-first.** You write a `.proto` file, both sides generate stubs from it. The contract is enforced at compile time, not discovered at runtime when a field is missing. This catches the entire class of "producer added a field, consumer's DTO silently drops it" bugs before deploy.
- **Binary (Protobuf), not JSON.** Smaller payloads, faster (de)serialization. Matters at high call volume between services — matters less for a handful of calls per request.
- **HTTP/2 by default** — multiplexed streams over one connection, so you're not paying a new TCP+TLS handshake per call the way naive REST clients sometimes do.
- **Four call shapes, not one.** Unary (request→response, same as REST), server streaming, client streaming, and bidirectional streaming. The streaming shapes are the actual reason to reach for gRPC over REST+HTTP/2 — if you never need a stream, the gain over REST is mostly the strict typing.

**Where it bites:**

- Debugging is worse. `curl` and a browser dev-tools tab don't work against a binary protocol — you need `grpcurl` or a proper client, and error messages from misconfigured TLS/routing are less friendly than an HTTP 4xx.
- Browsers can't call gRPC directly (no gRPC-Web without a proxy layer like Envoy). So it's an internal-traffic tool, not a way to talk to your Angular frontend — REST or GraphQL stays at the edge, gRPC stays behind the gateway.
- Proto file versioning is its own discipline: never renumber a field, never change a field's type, deprecate rather than delete. Get this wrong across a service boundary and you get silent data corruption, not a clean error — worse than a REST client choking on an unexpected JSON shape.

**Spring-specific note:** `grpc-spring-boot-starter` (the community one, not an official Spring project — check who maintains the version you pin) wires a `@GrpcService` bean into Spring's DI container and gets you server reflection for free, which makes `grpcurl` usable without shipping the `.proto` separately. Worth it once you're past three or four internal gRPC services; not worth the setup for one.

## GraphQL — for client-shaped responses

The problem GraphQL actually solves: a REST resource returns a fixed shape, and different clients (mobile app, admin back-office, public widget) want different subsets and different nesting depths of the same underlying data. Without GraphQL you either over-fetch (send the mobile client fields it discards) or build bespoke endpoints per client (`/mobile/notes`, `/admin/notes`) that drift out of sync.

GraphQL replaces that with one endpoint and a query the client writes:

```graphql
query {
  note(id: 42) {
    title
    author { name }
  }
}
```

The client gets exactly `title` and `author.name`, nothing else. No endpoint proliferation, no versioning by URL — the schema evolves by adding fields, and deprecated fields get a `@deprecated` directive instead of a breaking removal.

**The trap almost everyone hits first: N+1.** A naive resolver for `note.author` runs one query per note in a list. Fetch 50 notes, get 51 database round trips. This isn't a GraphQL flaw exactly — it's what happens when you resolve fields independently without batching. Fix it with a dataloader/batching layer (Spring for GraphQL's `BatchLoaderRegistry`, or DataLoader in the Node ecosystem) that collapses per-item lookups into one batched query per request. Ship GraphQL without this and it will look fine in dev with 3 test rows and fall over in production.

**Where it genuinely helps in a microservices context:** GraphQL as a **federation/BFF layer in front of several backend services** — one graph, several resolvers each hitting a different downstream service (possibly over the gRPC you set up internally). This is the combination that shows up in mature platforms: gRPC between services, GraphQL (or a plain BFF) at the edge assembling what each frontend actually needs. Apollo Federation and Spring for GraphQL both support this pattern; the cost is an extra layer to operate and reason about, so it's worth it once you have more than two or three frontends with meaningfully different data needs, not before.

**Where it doesn't help:** simple CRUD with one client. REST is less machinery, easier to cache at the HTTP layer (GraphQL's single POST endpoint breaks naive HTTP caching — you need field-level or persisted-query caching instead), and easier for a new engineer to understand from the outside.

## Picking one — the actual decision

| Situation | Pick |
|---|---|
| Public/partner API, single well-known response shape | REST |
| Service-to-service inside the platform, high call volume or streaming | gRPC |
| One backend, several frontends with different data-shape needs | GraphQL (as BFF) |
| Simple CRUD, one client, small team | REST — don't add machinery you don't need |
| You're not sure | REST. It's the one every engineer who joins the team already knows. |

The failure mode to actually watch for isn't picking the "wrong" one — it's picking a second one on top of REST for a problem REST already handled fine, because a conference talk made it sound necessary. Every protocol you add is a second thing to operate, secure, monitor, and onboard people onto. Add gRPC when the proto-first contract and the streaming shapes solve a real internal-traffic problem you have today. Add GraphQL when over-fetching or endpoint proliferation is an actual, measured pain — not preemptively.
