# Telecom Network Architecture

The network is three layers stacked: **Access** (radio) → **Transport** (moving bits) → **Core** (intelligence). Plus OSS/BSS on the side running the business.

## 1. RAN — Radio Access Network

Where the device meets the network.

```
UE (phone) ─► Antenna + Radio (RU) ─► Baseband (DU/CU) ─► toward the core
```

- **4G**: the base station is the **eNodeB**.
- **5G**: it's the **gNodeB**, often split into **CU** (centralized unit), **DU** (distributed unit), **RU** (radio unit) — the "functional split" that enables Open RAN.
- **Open RAN (O-RAN)**: standardized open interfaces so operators can mix vendors (radio from A, baseband from B) instead of one locked stack. Big industry shift.

## 2. Transport / Backhaul

The plumbing connecting thousands of cell sites back to the core: fiber, microwave links, sometimes satellite. Split into:
- **Fronthaul**: RU ↔ DU (very high bandwidth, tight latency)
- **Midhaul**: DU ↔ CU
- **Backhaul**: CU ↔ Core

## 3. Core Network — the brain

### 4G core (EPC — Evolved Packet Core)

| Element | Role |
|---|---|
| **MME** | Mobility & session management, authentication signaling |
| **HSS** | Subscriber database (identities, keys, profiles) |
| **S-GW** | Serving gateway — routes user data packets |
| **P-GW** | Packet gateway — the exit to the internet; enforces policy, does charging |
| **PCRF** | Policy & charging rules (QoS, quotas) |

### 5G core (5GC — Service-Based Architecture)

5G rebuilt the core as **cloud-native microservices** talking over HTTP/2 APIs. Same jobs, new shape:

| 4G element | 5G equivalent | Job |
|---|---|---|
| MME | **AMF** | Access & mobility management |
| — | **SMF** | Session management |
| S-GW/P-GW (user data) | **UPF** | User Plane Function — moves the actual packets |
| HSS | **UDM / UDR** | Unified data management |
| PCRF | **PCF** | Policy control |
| — | **NRF** | Network Repository — service discovery (like Eureka!) |
| — | **NEF** | Exposes network capabilities to external APIs |

**Control plane / user plane separation (CUPS)** is the key idea: signaling ("set up a session") is decoupled from data forwarding ("push these packets fast"). The **UPF** can sit at the network edge for low latency while control stays centralized.

> If you know microservices, 5GC will feel familiar: stateless network functions, a service registry (NRF), API-based communication, horizontal scaling on Kubernetes. Telephony became a distributed system. See [../devsecops/containers/kubernetes-essentials.md](../devsecops/containers/kubernetes-essentials.md).

## 4. IMS — the voice/multimedia layer

**IP Multimedia Subsystem** delivers voice and messaging over IP (VoLTE, VoWiFi, VoNR). Built on **SIP** signaling ([protocols](protocols.md)). It's why a 4G/5G "call" is really a managed IP session with guaranteed quality.

## Where the money logic lives

The **P-GW (4G)** / **UPF + SMF + PCF (5G)** are where usage is measured and policy enforced — the bridge to real-time [billing](billing/). Every megabyte that counts against your plan is counted here.
