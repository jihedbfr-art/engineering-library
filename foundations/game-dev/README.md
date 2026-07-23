# 🎮 Game Development

A different discipline from most of this library — instead of request/response and eventual consistency, games run a tight real-time loop where every millisecond of frame budget matters and the "user" expects 60 frames of consistent state every second, no exceptions.

- [game-engine-architecture.md](game-engine-architecture.md) — the game loop, ECS (Entity-Component-System), engine layers
- [graphics-fundamentals.md](graphics-fundamentals.md) — the rendering pipeline, from vertices to pixels
- [networking-multiplayer.md](networking-multiplayer.md) — client prediction, lag compensation, authoritative servers

## The mental shift coming from web/backend development

A web request lives for milliseconds and dies. A game's main loop lives for the entire session and must never miss its frame budget — 16.6ms for 60fps, no exceptions, no "it's fine, it'll just be a bit slower this request." That single constraint (a hard, continuous real-time deadline instead of a soft, per-request one) shapes almost every architectural decision in this section: why games avoid garbage-collection pauses, why they use fixed-size object pools instead of allocating freely, why networking optimizes for smoothing over unreliable, high-latency connections instead of just retrying.
