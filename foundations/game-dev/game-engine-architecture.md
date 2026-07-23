# Game Engine Architecture

## The game loop — the heartbeat of everything

```
while (running) {
    processInput();        // read keyboard/mouse/gamepad/network
    update(deltaTime);      // advance game state — physics, AI, logic
    render();                // draw the current state to the screen
}
```
Every game, from Pong to a AAA open-world title, runs this loop at its core. The engineering challenge is making `update` and `render` consistently fit inside your frame budget (16.6ms at 60fps, 8.3ms at 120fps) — miss it once and the player perceives a stutter; miss it often and the game feels broken, no matter how good the content is.

## Fixed vs variable timestep — the decision that avoids a whole class of bugs

```cpp
// Variable timestep — simple, but physics behaves differently at different framerates
update(deltaTime);   // deltaTime varies: fast on a good PC, slow on a weak one

// Fixed timestep — the standard for anything physics-sensitive
accumulator += frameTime;
while (accumulator >= FIXED_STEP) {          // FIXED_STEP e.g. 1/60s, always
    update(FIXED_STEP);                       // physics always advances by the same amount
    accumulator -= FIXED_STEP;
}
render(interpolate(accumulator / FIXED_STEP)); // render smoothly between fixed steps
```
Variable timestep means the same jump, at the same input, covers a different distance on a fast machine vs a slow one — physics literally behaves differently per player, which is unacceptable the moment gameplay (not just visuals) depends on it, and especially unacceptable in multiplayer where players' simulations must agree. Fixed timestep with interpolated rendering is the standard fix: game logic always advances in identical, deterministic chunks; only the *visual smoothness* between those chunks depends on framerate.

## ECS — Entity-Component-System

The dominant modern architecture pattern, replacing deep inheritance hierarchies (`GameObject → Character → Player`) that get unwieldy fast once you need a "flying, invisible, poisoned NPC" without a combinatorial explosion of subclasses.

```
Entity:      just an ID. No data, no behavior — a Player is entity #42, nothing more.

Component:   pure data, no logic.
  Position { x, y, z }
  Velocity { dx, dy, dz }
  Health    { current, max }
  Renderable{ mesh, texture }

System:      pure logic, operates on all entities that have a specific set of components.
  MovementSystem:  for every entity with (Position, Velocity) → update Position
  RenderSystem:    for every entity with (Position, Renderable) → draw it
  HealthSystem:    for every entity with (Health) → check for death, regen, etc.
```
```
Want a flying, poisoned, invisible NPC?
Just attach the Flying, Poisoned, and Invisible components to that entity.
No new class, no inheritance decision, no combinatorial subclass explosion.
```
The composition-over-inheritance win here is real and specific: behavior is defined by **which components an entity has**, not by which class it descends from — adding a new capability to some entities never requires touching an existing class hierarchy. It's also cache-friendly: systems iterate over tightly-packed arrays of one component type at a time (all Positions together in memory), which is dramatically faster on modern CPUs than chasing pointers through a scattered object-oriented hierarchy — a genuinely different reason to prefer it beyond just code organization.

## Engine layers — how it's actually organized

```
Game-specific logic (your actual gameplay code)
        ↓
Gameplay framework (ECS, scene graph, scripting hooks)
        ↓
Core engine systems (rendering, physics, audio, input, networking)
        ↓
Platform abstraction (graphics API, OS, file I/O — hides Windows vs console vs mobile)
```
This layering is why "build your own engine" and "use Unity/Unreal/Godot" is a real, consequential choice — an existing engine gives you the bottom three layers, extensively battle-tested across thousands of shipped games, for free; building your own means owning that entire stack yourself, worth it mainly when you need control an existing engine genuinely can't give you (a very specific rendering technique, an unusual platform, or a company betting its whole pipeline on proprietary tech).

## Scene graph vs ECS — not actually opposed

Older/simpler engines organize the world as a tree (a scene graph: parent-child transforms, a car entity owning wheel entities as children). ECS doesn't replace this outright — most modern engines use **both**: a scene graph or spatial hierarchy for transforms/hierarchy, and ECS for gameplay logic layered on top. Don't treat them as mutually exclusive architectural choices; they solve different problems.

## Frame budget breakdown — where the 16.6ms actually goes

```
Input processing         ~0.5ms
Game logic / AI update   ~3-5ms
Physics simulation       ~2-4ms
Rendering (CPU side)     ~3-5ms   — see graphics-fundamentals.md for GPU side
Audio                    ~0.5ms
Networking (if online)   ~1-2ms
                          ─────
Total budget: 16.6ms (60fps) — every system above competes for a slice of it
```
Profiling — literally measuring where each frame's milliseconds actually go — is not optional polish in game development the way it can be in a typical web backend; it's a core, continuous discipline, because a single system quietly exceeding its slice degrades the whole game's feel in a way users notice immediately, unlike a web API where an extra 50ms on one endpoint often goes unnoticed.

## Where this connects

[Graphics fundamentals](graphics-fundamentals.md) is what happens inside the render step above. [Multiplayer networking](networking-multiplayer.md) is what happens when the "process input" and "update" steps have to also account for other players' inputs arriving late, out of order, or not at all — a very different networking problem from typical [REST API](../backend/apis/rest-api-design.md) design.
