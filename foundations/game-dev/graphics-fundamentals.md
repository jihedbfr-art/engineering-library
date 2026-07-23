# Graphics Fundamentals — The Rendering Pipeline

How a 3D scene description becomes the pixels you actually see on screen, roughly 60+ times per second.

## The pipeline, stage by stage

```
3D Model Data (vertices, in "model space")
        ↓  Vertex Shader — transforms each vertex: model → world → view → clip space
Vertices in clip space
        ↓  Rasterization — GPU fixed-function hardware converts triangles into pixels ("fragments")
Fragments (candidate pixels, with interpolated data)
        ↓  Fragment/Pixel Shader — computes the final color for each fragment (lighting, textures)
Colored fragments
        ↓  Depth testing, blending — resolves overlapping fragments, transparency
Final pixel color → written to the frame buffer → displayed
```
The two shader stages are where you write actual code (vertex shaders and fragment shaders, in GLSL/HLSL); rasterization in between is fixed-function GPU hardware you don't program directly — understanding *where* your control starts and stops in this pipeline is the first real "click" moment for anyone learning graphics programming.

## The coordinate space journey — why a vertex gets transformed four times

```
Model space    — coordinates relative to the object itself (a cube centered at origin)
        ↓ × Model matrix (position/rotate/scale the object in the world)
World space    — coordinates relative to the whole scene
        ↓ × View matrix (transform relative to the camera)
View space     — coordinates relative to the camera's position/orientation
        ↓ × Projection matrix (perspective or orthographic)
Clip space     — ready for the GPU to determine what's actually visible
```
This four-matrix chain (Model, View, Projection, and the implicit screen-space mapping after) is the mathematical backbone of essentially every 3D renderer that exists — once you genuinely understand why each transform exists (object placement, camera perspective, lens/projection), the same mental model applies whether you're using a low-level API or a high-level engine that hides it from you.

## Rasterization — turning triangles into pixels

The GPU takes each triangle (three vertices in clip space, now mapped to screen coordinates) and determines exactly which pixels it covers, interpolating vertex data (color, texture coordinates, normals) smoothly across each covered pixel. This is done in dedicated, fixed-function GPU hardware for a specific reason: it's an extremely regular, parallelizable computation, and offloading it from programmable shader cores to specialized hardware is a huge chunk of why modern GPUs can rasterize millions of triangles per frame.

## Lighting models — from cheap to physically accurate

```glsl
// Simplified Phong lighting — the classic starting point
vec3 ambient  = ambientColor * materialColor;
vec3 diffuse  = max(dot(normal, lightDir), 0.0) * lightColor * materialColor;
vec3 specular = pow(max(dot(reflectDir, viewDir), 0.0), shininess) * lightColor;
vec3 finalColor = ambient + diffuse + specular;
```
- **Phong/Blinn-Phong**: cheap, "good enough" approximation — ambient (fill light), diffuse (matte surface response), specular (shiny highlight). Still common where performance matters more than photorealism.
- **PBR (Physically Based Rendering)**: models real material properties (roughness, metalness) using actual light-transport approximations rather than artist-tuned coefficients — the current standard for anything aiming at realism, at real GPU cost.

## Textures, mipmapping, and the problem they solve

A texture is just an image mapped onto a surface via UV coordinates. **Mipmapping** pre-generates progressively smaller versions of each texture (half-size, quarter-size, ...) specifically so that a distant object samples an appropriately low-resolution version instead of a full-resolution one — without this, distant textured surfaces produce ugly flickering/aliasing as the camera moves, because a full-res texture sampled sparsely at a distance essentially samples noise.

## The CPU/GPU split, and why it matters for performance

```
CPU: game logic, physics, decides WHAT to draw and issues "draw calls"
        ↓ (draw calls — a real, measurable cost per call)
GPU: actually rasterizes and shades everything, in massively parallel hardware
```
**Draw call count is a classic, very real performance bottleneck** — each draw call has CPU-side overhead regardless of how simple the object is, so a scene with 10,000 tiny separate draw calls can be dramatically slower than the same visual content batched into far fewer, larger draw calls. This is exactly why "batching"/"instancing" are constant, recurring topics in real-time graphics optimization — it's addressing this specific, measurable cost, not a vague performance platitude.

## Real-time constraints vs offline rendering (the distinction that explains a lot of tradeoffs)

Film-quality ray tracing can spend hours per frame; a game has ~16ms, total, for everything. This single constraint explains why games have historically favored rasterization (fast, approximate) over ray tracing (slow, physically accurate) — and why modern **hardware-accelerated real-time ray tracing** (RTX-class GPUs) is such a genuinely significant shift: it's the first time ray-traced effects (real reflections, real global illumination) have become affordable inside that same brutal 16ms budget, rather than requiring an offline render farm.

## Where this connects

This is the render step inside the [game engine's frame loop](game-engine-architecture.md) — everything above happens inside that single `render()` call, within its slice of the 16.6ms frame budget.
