# Multiplayer Networking — Client Prediction, Lag Compensation, Authority

Real-time multiplayer networking solves a fundamentally different problem than [typical web/API networking](../backend/apis/rest-api-design.md): instead of one request-response exchange tolerant of a few hundred milliseconds, you need dozens of players' inputs to converge on a consistent, cheat-resistant shared reality, updated many times per second, over an unreliable internet connection.

## The core problem: the speed of light is not negotiable

A player in Tunisia and a player in Japan cannot have zero-latency communication — physics forbids it. Every multiplayer architecture is fundamentally a set of strategies for **hiding** that unavoidable latency from the player's perception, not eliminating it.

## Client-server authoritative model — the standard architecture

```
Player's client:  sends INPUT (not game state) to the server
                   "I pressed forward, I pressed fire"
        ↓
Server:            the single source of truth. Simulates the real game state
                   from all players' inputs, resolves conflicts, detects cheating
        ↓
All clients:       receive the server's authoritative STATE, render it
```
**The server decides what actually happened; clients only send intent.** This is the standard defense against the most basic cheating vector: if a client could simply tell the server "I dealt 500 damage," a cheater's job is trivial. If the client can only say "I pressed the fire button" and the server independently simulates whether that shot actually hit, cheating requires attacking the server's simulation logic itself — a much higher bar. The same "never trust the client" principle from [web security](../../cybersecurity/web-security.md), applied to real-time gameplay instead of HTTP requests.

## Client-side prediction — hiding latency for the local player

If a client waited for the server's round trip before showing the result of your own input, every action would feel laggy and unresponsive — unacceptable for anything action-oriented. Instead:

```
1. Player presses "move forward"
2. Client IMMEDIATELY simulates the movement locally and renders it — feels instant
3. Client also sends the input to the server
4. Server simulates authoritatively, sends back the real result
5. Client compares its own prediction to the server's authoritative result:
   - Match?     Nothing visible happens — the prediction was correct
   - Mismatch?  "Reconciliation" — snap/smoothly correct to the server's real state
```
This is why, occasionally, you'll see a character briefly "rubber-band" backward in a laggy match — that's reconciliation correcting a wrong local prediction, made visible. The alternative (no prediction at all) is uniformly, constantly laggy input for everyone; occasional visible correction is the better tradeoff almost every real-time game makes.

## Lag compensation — making hits feel fair despite latency

```
Player A shoots at Player B's visible position on Player A's own screen.
Due to latency, what A sees is actually B's position from ~100ms ago —
B has already moved, on the server's real, current timeline.

Server-side lag compensation: rewind the server's simulation back to
"what did the world look like from A's perspective when A fired?"
and resolve the hit against THAT historical state, not the current one.
```
Without this, every hit registration would be biased against high-latency players in a way that feels deeply unfair — you visually land a shot, and the server (working from stale information) says you missed. Lag compensation is what makes "I clearly hit them" mostly match "the server agrees you hit them" — at the cost of a genuinely interesting tradeoff: it can occasionally produce "shot around a corner" moments, where a high-latency shooter's rewound view briefly still shows a target the low-latency victim had already ducked behind on their own screen. Every competitive shooter's netcode is, underneath, a specific set of choices about exactly this tradeoff.

## Interpolation — smoothing other players' movement

```
Server sends state updates at, say, 20 times/second (a "tick rate" of 20Hz) —
far below the 60fps+ you're rendering at.

Naive rendering: other players visibly teleport/stutter between each update.

Interpolation: render other players slightly IN THE PAST (e.g. 100ms behind
real-time) and smoothly interpolate between the last two known server
states — trading a small amount of extra visual latency for the ability to
render other players' movement smoothly, instead of jerkily jumping between
sparse updates.
```
This is the standard technique for making the (necessarily lower-frequency) network update rate look like smooth 60fps motion for entities you don't control locally — the small added lag is a deliberate, worthwhile trade for visual smoothness.

## Tick rate — the fundamental quality/cost tradeoff

```
Higher tick rate  → more responsive, more accurate hit detection,
                     genuinely fairer gameplay → but proportionally
                     more server CPU and bandwidth per player, meaning
                     fewer concurrent matches per server, higher cost
Lower tick rate   → cheaper to run at scale → less precise, and every
                     competitive player will eventually notice and complain
```
This single number is one of the most consequential, most visibly-argued-about decisions in any competitive multiplayer game's server architecture — it's a direct, unavoidable cost-vs-fairness tradeoff, not a purely technical one, which is exactly why competitive communities scrutinize a game's server tick rate so closely.

## State synchronization strategies

| Approach | How | Best for |
|---|---|---|
| **Full state snapshot** | Send complete world state each tick | Simple, but bandwidth-heavy at scale |
| **Delta compression** | Send only what changed since the last acknowledged state | Standard for competitive shooters — far less bandwidth |
| **Event-based** | Send discrete events ("player X fired"), not continuous state | Turn-based or lower-frequency games |

## Where this connects

This is a specialized, much harder application of the same [event-driven, low-latency thinking](../../data-engineering/streaming-kafka.md) and "never trust the client" [security principle](../../cybersecurity/web-security.md) that show up elsewhere in this library — multiplayer netcode is just that problem under a genuinely unforgiving, continuous real-time deadline instead of a request-by-request one, which is what makes it its own specialized discipline.
