# VoLTE & IMS — Voice in the All-IP Era

When networks went all-IP (4G), voice couldn't ride the old circuit-switched path anymore. **IMS** is the framework that carries voice/video as managed IP sessions; **VoLTE** is voice delivered over it on LTE (VoNR on 5G, VoWiFi over Wi-Fi).

## Why IMS exists

Before 4G, voice used a dedicated circuit (guaranteed quality by design). LTE is packet-only — so voice became an app that needs *engineered* quality. IMS provides:
- session control (set up / modify / tear down calls) via **SIP** ([protocols](protocols.md))
- guaranteed **QoS** (a dedicated bearer so your call doesn't stutter when the network is busy)
- interoperability, emergency calls, supplementary services (call waiting, conferencing), and interconnect to other networks

## IMS core components

| Component | Role |
|---|---|
| **P-CSCF** | Proxy CSCF — the first contact point for the device; secures the SIP signaling |
| **I-CSCF** | Interrogating CSCF — finds the right S-CSCF for a user |
| **S-CSCF** | Serving CSCF — the brain: registration, session control, service triggering |
| **HSS** | Subscriber data & authentication (shared with the core) |
| **AS** (Application Server) | Hosts the actual services (telephony features, voicemail) |
| **MGCF / MGW** | Gateways to legacy circuit-switched networks |

```
Phone ──SIP──► P-CSCF ──► I-CSCF ──► S-CSCF ──► Application Servers
                                        │
                                       HSS (who you are, what you're allowed)
```

## The VoLTE call lifecycle

1. **Registration**: on attach, the device registers with IMS (SIP REGISTER), authenticated via the SIM.
2. **Session setup**: SIP INVITE negotiates codecs and media (see the SIP flow in [protocols](protocols.md)).
3. **Dedicated bearer**: the network sets up a **QCI-1** bearer — a guaranteed-bitrate path just for the voice media (RTP), so quality holds under load.
4. **Media**: voice flows as RTP packets, typically with the **AMR-WB / EVS** codecs (HD Voice).
5. **Teardown**: SIP BYE releases the session and the bearer.

## Benefits over old circuit voice

- **HD Voice** (wideband/EVS codecs) — noticeably clearer.
- **Faster call setup** (often < 1s vs several seconds).
- **Simultaneous voice + high-speed data** (no dropping to 3G for a call).
- One converged IP network to run instead of two.

## The variants

| Name | Voice over... |
|---|---|
| **VoLTE** | LTE (4G) |
| **VoNR** | 5G New Radio (standalone 5G) |
| **VoWiFi** | any Wi-Fi (calls over your home broadband; great for poor indoor coverage) |
| **RCS** | Rich Communication Services — "SMS++" (typing indicators, media, read receipts) over IMS; the carrier answer to messaging apps |

## Fallbacks (what happens when VoLTE isn't available)

- **CSFB (Circuit-Switched Fallback)**: 4G device drops to 2G/3G to make a call when VoLTE isn't deployed.
- **EPS Fallback**: 5G device falls back to 4G VoLTE when VoNR isn't available. Extremely common today — most "5G" voice is actually VoLTE via EPS fallback.

## Security angle

- SIP signaling is protected (IPsec/TLS between device and P-CSCF).
- IMS authentication uses the SIM credentials (AKA) — same trust root as network attach.
- VoWiFi tunnels over untrusted Wi-Fi via IPsec to the operator's ePDG gateway.
- Same defensive mindset as any [session/auth system](../cybersecurity/web-security.md): authenticate, encrypt, protect the signaling plane.

## For engineers

If SIP status codes (`180 Ringing`, `200 OK`, `486 Busy`) look like HTTP to you — they should. IMS is, at heart, a **distributed session-control system** with QoS guarantees and a telco trust model. VoIP/SIP skills transfer directly; the telco-specific parts are QoS bearers, SIM-based auth, and interconnect.
