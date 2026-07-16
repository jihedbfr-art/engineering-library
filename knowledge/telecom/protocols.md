# Telecom Protocols & Signaling

"Signaling" = the control messages that set up, manage and tear down calls and sessions (as opposed to the actual voice/data payload). This is where telecom's unique protocols live.

## The signaling map by generation

| Protocol | Era / Layer | Purpose |
|---|---|---|
| **SS7** | 2G/3G core signaling | Call setup, SMS, roaming, number translation |
| **SIGTRAN** | SS7 over IP | Carries legacy SS7 messages over IP networks |
| **Diameter** | 4G/LTE | Authentication, authorization, accounting (AAA); policy & charging |
| **GTP** | 3G/4G | Tunnels user data through the core (GTP-U) + control (GTP-C) |
| **SIP** | IMS / VoLTE / VoIP | Sets up multimedia sessions (voice, video) |
| **HTTP/2 + JSON** | 5G core (SBA) | Network functions talk over REST-like APIs |

Note the arc: from telecom-specific (SS7) → to IP-based (Diameter, SIP) → to **plain web tech (HTTP/2, JSON, TLS, OAuth2)** in 5G. Telecom converged onto the internet stack.

## SS7 — the old backbone (and its ghosts)

Built in the 1970s–80s for a trusted club of state operators. It works, still carries huge 2G/3G traffic and roaming.
- **The problem**: it assumes every peer is trustworthy — no real authentication. Once attackers got access, SS7 flaws allowed **location tracking, call/SMS interception, and OTP theft**. A landmark reason SMS is a weak second factor ([why](../cybersecurity/web-security.md)).
- Mitigations: SS7 firewalls, and the industry move to Diameter/5G with real security.

## Diameter — the 4G AAA workhorse

The successor to RADIUS (hence the name — a diameter is twice a radius 😄). In LTE it handles:
- **Authentication** of subscribers (via HSS)
- **Policy & charging** (PCRF ↔ gateways)
- **Online/offline charging** interfaces to the billing system (see [billing](billing/))

Diameter inherited some of SS7's inter-operator trust issues — Diameter firewalls exist for the same reasons.

## GTP — carrying user data

**GPRS Tunneling Protocol** wraps subscriber traffic so it can move through the mobile core independently of the underlying IP transport.
- **GTP-C**: control (create/modify/delete sessions)
- **GTP-U**: user data tunnel (your actual packets)

## SIP — how modern "calls" work

**Session Initiation Protocol** is text-based and HTTP-like. It sets up, modifies and ends media sessions; the media itself flows over **RTP**.

```
Caller                     Proxy/IMS                    Callee
  │──── INVITE ──────────────►│──── INVITE ──────────────►│
  │◄─── 100 Trying ───────────│                           │
  │                           │◄─── 180 Ringing ──────────│
  │◄─── 180 Ringing ──────────│                           │
  │◄─── 200 OK ───────────────│◄─── 200 OK ───────────────│
  │──── ACK ──────────────────────────────────────────────►│
  │════════════ RTP media (voice/video) ══════════════════│
  │──── BYE ──────────────────────────────────────────────►│
```
VoLTE, VoWiFi, VoNR, and most VoIP (including corporate PBXs and services) all speak SIP. If you've seen HTTP status codes, SIP's `180 Ringing` / `200 OK` / `486 Busy Here` will feel oddly familiar.

## VoLTE / VoNR in one line

Voice as a guaranteed-quality IP session over the IMS, instead of a legacy circuit. It's why 4G/5G voice can coexist with data on the same all-IP network.

## For developers: the API era (CAMARA)

The newest layer exposes network capabilities as **plain REST APIs** — SIM swap detection, device location, quality-on-demand, number verification. Standardized by the **CAMARA** project (Linux Foundation) + GSMA Open Gateway. You call telecom features like any SaaS API. More in [oss-bss](oss-bss.md).
