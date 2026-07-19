# Multi-access Edge Computing (MEC)

Compute pushed to the edge of the mobile network — a few milliseconds from the radio, instead of a few hundred from a centralized cloud region. The piece that turns "5G is fast" into "5G is fast *and* the app runs close enough that speed matters."

## The problem it solves

5G radio can push latency down to single-digit milliseconds at the air interface, but that gain evaporates if every request still round-trips to a data center three hops and a few hundred kilometers away. MEC puts compute — a small data center, sometimes a handful of servers — at or near the base station / aggregation site, so the application server for a latency-sensitive workload sits inside the operator's network instead of behind it.

## Where it sits, architecturally

```
UE → RAN → [MEC host, at or near the edge site] → Core network → Internet / centralized cloud
```

The MEC host intercepts traffic before it goes deep into the core — in a 5G Service-Based Architecture, this is done via the **UPF (User Plane Function)** being placed at the edge and steering matching traffic locally instead of backhauling it, via a **local breakout**. See [network-architecture.md](network-architecture.md) for where UPF sits in the broader core.

## What actually runs on MEC

- **Video analytics at the edge** — a camera feed processed locally instead of streamed whole to a data center; only results/alerts leave the site.
- **AR/VR offload** — rendering-heavy workload split so the headset stays light and the latency-critical part runs a few milliseconds away, not across the country.
- **Industrial automation** — closed-loop control (robotics, machine vision) that can't tolerate the round-trip time to a centralized cloud; pairs naturally with [private 5G](private-5g.md) on a factory floor.
- **Local CDN-like caching** — popular content cached at the edge site, same idea as a traditional CDN but tied to the mobile network topology rather than internet peering points.
- **Network-native apps** — operators exposing MEC as a platform (GSMA Operator Platform work) so third-party developers can deploy workloads onto edge sites without negotiating with each operator individually — the same "write once" ambition as [CAMARA](camara-network-apis.md), applied to compute instead of network capability APIs.

## MEC vs plain "edge computing"

Generic edge computing (a server in a retail store, a gateway on a factory floor) isn't tied to the mobile network. MEC specifically means the compute is integrated *into* the operator's infrastructure — which is what gives it two things a generic edge box doesn't have: guaranteed proximity to a given radio cell, and awareness of network context (radio conditions, location, QoS state) that the application can actually query and react to.

## Standardization

ETSI MEC defines the reference architecture and APIs (radio network information, location, bandwidth management) that let applications query network context from the edge host itself — a video analytics app can, for instance, ask the platform for the current radio conditions of a specific device and adapt bitrate accordingly, rather than inferring it indirectly.

## Why this matters beyond telecom people

For a backend developer, a MEC deployment often just looks like "deploy this container to a different, closer Kubernetes cluster" — the edge sites increasingly run standard container orchestration, not bespoke telecom middleware. The telecom-specific part is *which* cluster is closest to a given user, and that's decided by network topology the app usually doesn't need to know about directly, only the deployment/routing layer does.

## Related

- [private-5g.md](private-5g.md) — MEC is one of the deployment patterns that make private 5G compelling for industrial use cases
- [5g.md](5g.md) — network slicing and the broader 5G core context MEC sits inside
- [network-architecture.md](network-architecture.md) — where UPF and local breakout fit in the three-layer model
