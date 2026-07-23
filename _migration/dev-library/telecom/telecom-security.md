# Telecom Security

Securing the networks that everything else runs on. Telecom is **critical national infrastructure** — an attractive target for criminals and nation-states alike. This page bridges [telecom](README.md) and [cybersecurity](../cybersecurity/README.md).

## The attack surface, layer by layer

| Layer | Threats |
|---|---|
| **Radio (RAN)** | IMSI catchers / fake base stations, jamming, downgrade attacks |
| **Signaling (SS7/Diameter)** | Location tracking, call/SMS interception, OTP theft, fraud |
| **Core / SBA** | Misconfigured network functions, API abuse, lateral movement |
| **Roaming / interconnect** | Cross-operator trust abuse, roaming fraud |
| **Subscriber (SIM)** | SIM swap, SIM cloning (legacy), eSIM provisioning attacks |
| **Devices / IoT** | Botnets (Mirai), weak credentials, compromised endpoints |
| **OSS/BSS** | Data breaches (subscriber PII), billing fraud, insider threat |

## The SS7/Diameter problem (telecom's original sin)

Legacy signaling ([protocols](protocols.md)) assumed a **closed club of trusted operators** — no authentication between networks. Once attackers gained signaling access (rentable on grey markets), they could:
- **Track a phone's location** anywhere in the world
- **Intercept calls and SMS** (including banking OTPs)
- **Commit fraud** via roaming and routing manipulation

This is the concrete reason **SMS is a weak second factor** — an attacker with SS7 access can silently receive your OTP. Prefer app-based/hardware MFA ([web security](../cybersecurity/web-security.md)).

**Defenses**: SS7/Diameter **signaling firewalls**, filtering of dangerous message categories, GSMA security guidelines (FS.11 etc.), and migration to 5G's authenticated interfaces.

## Fake base stations (IMSI catchers / "stingrays")

A rogue base station tricks nearby phones into connecting, then identifies/tracks them or forces a downgrade to weaker 2G encryption.
- **4G/5G improvements** make this harder; **5G encrypts the subscriber identity (SUCI)** so the IMSI isn't broadcast in the clear — a major privacy win.

## 5G security — what got better

| Improvement | Effect |
|---|---|
| **SUCI (concealed identity)** | Defeats IMSI catchers — no plaintext IMSI over the air |
| **5G-AKA** | Stronger mutual authentication |
| **SBA uses TLS + OAuth2** | Network functions authenticate like modern web services |
| **SEPP** | Security Edge Protection Proxy — protects roaming/interconnect signaling |
| **Enhanced home control** | Home network verifies the device is really where the visited network claims |

5G's control plane security is, essentially, **web-API security** (TLS, OAuth2, JWT) applied to telephony — the same primitives in [web-security](../cybersecurity/web-security.md).

## SIM swap — where telecom meets fintech fraud

An attacker social-engineers the operator into porting a victim's number to a new SIM, then receives their OTPs and takes over bank/email accounts.
- **Defenses (operator side)**: stronger port-out verification, port-freeze options, staff anti-social-engineering training.
- **Defenses (app side)**: use the **SIM Swap API** ([CAMARA](camara-network-apis.md)) to detect a recent swap and block risky actions; move off SMS OTP.

## New 5G/virtualization risks

Cloud-native telecom inherits cloud-native risks:
- **Container/K8s security** for the virtualized core → [kubernetes-essentials](../devsecops/containers/kubernetes-essentials.md), [container hardening](../devsecops/containers/docker-hardening.md)
- **API security** on the service-based interfaces → [SAST/DAST](../devsecops/security/sast-dast.md), [OWASP](../devsecops/security/owasp-top10.md)
- **Supply chain**: who builds the network functions (a reason behind vendor restrictions) → [secrets & supply chain](../devsecops/security/secrets-management.md)
- **Open RAN**: more interfaces and vendors = broader attack surface to secure.

## Regulation & standards

- **GSMA** security guidelines & the **NESAS/SCAS** scheme for testing network equipment.
- National critical-infrastructure rules; lawful-interception obligations.
- **Supply-chain regulation** (equipment from "high-risk vendors" restricted in several countries).

## For defenders

Telecom security is [blue-team](../cybersecurity/blue-team.md) at national scale:
- monitor signaling for anomalies (a location-tracking probe looks like recon)
- correlate fraud + security (the same SIM-swap event is both)
- apply defense-in-depth and least privilege ([fundamentals](../cybersecurity/fundamentals.md)) from radio to billing
- assume every interconnect peer is potentially hostile — the SS7 lesson, learned the hard way.
