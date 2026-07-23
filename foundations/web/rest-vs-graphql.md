# REST vs GraphQL vs gRPC

Three ways to build APIs. None is "best" — each fits different problems.

## REST — resources over HTTP

The default. Resources (`/notes`, `/users/7`) manipulated with HTTP verbs. See [REST API design](../backend/apis/rest-api-design.md).

✅ Simple, cacheable (HTTP caching just works), universally understood, great tooling.
⚠️ **Over-fetching** (you get fields you don't need) and **under-fetching** (need 3 calls to build one screen → N+1 requests).

```
GET /users/7           → user
GET /users/7/notes     → their notes
GET /notes/3/comments  → comments   (multiple round trips)
```

## GraphQL — ask for exactly what you need

A query language: the client specifies the shape, one endpoint returns exactly that.

```graphql
query {
  user(id: 7) {
    name
    notes(last: 5) {
      title
      comments { text }
    }
  }
}
```
One request, exactly the fields requested, arbitrary depth.

✅ No over/under-fetching, strongly typed schema, great for complex/varied frontends (mobile + web with different needs), self-documenting.
⚠️ Caching is harder (it's usually POST to one URL), easy to write expensive queries (need depth/complexity limits), server complexity, the N+1 problem moves server-side (solve with DataLoader batching).

## gRPC — fast service-to-service

Binary protocol (Protocol Buffers) over HTTP/2. You define the contract in `.proto`, generate typed client/server code.

```proto
service NotesService {
  rpc GetNote(GetNoteRequest) returns (Note);
}
```

✅ Very fast & compact, strongly typed, streaming, polyglot codegen — ideal **between microservices**.
⚠️ Not browser-native (needs a proxy), binary payloads aren't human-readable, steeper setup.

## How to choose

| Need | Reach for |
|---|---|
| Public API, simplicity, HTTP caching | **REST** |
| Rich clients with varied data needs, avoid round trips | **GraphQL** |
| Internal microservice-to-microservice, low latency | **gRPC** |

Real systems mix them: REST/GraphQL at the public edge, gRPC between internal services. Pick per boundary, not by fashion.
