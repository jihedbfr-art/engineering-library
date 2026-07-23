# IoT & M2M Connectivity

How billions of devices — meters, trackers, sensors, cars — get and stay connected over telecom networks. **M2M** (Machine-to-Machine) is the older term; **IoT** (Internet of Things) is the broader modern one.

## What makes IoT connectivity different from your phone

| Phone | Typical IoT device |
|---|---|
| High bandwidth | Tiny data (a few bytes/day for a meter) |
| Charged nightly | Must last **years** on a battery |
| Human-managed | Zero-touch, deployed by the thousands |
| Good coverage spot | Often deep indoors / underground / remote |

This drove purpose-built low-power network technologies.

## Cellular IoT technologies (3GPP)

| Tech | Profile | Use case |
|---|---|---|
| **NB-IoT** | Ultra-low power, low data, great deep-indoor coverage | Utility meters, parking sensors, agriculture |
| **LTE-M (Cat-M1)** | Low power, moderate data, supports mobility & voice | Asset trackers, wearables, alarms |
| **5G mMTC** | Massive device density (future evolution of the above) | Smart cities at scale |
| **5G URLLC** | Ultra-reliable, low latency | Industrial automation, V2X, robotics |

NB-IoT and LTE-M are the current workhorses — they reuse licensed spectrum and existing towers, so operators deploy them via software upgrades.

## Non-cellular alternatives (context)

- **LoRaWAN**, **Sigfox** — unlicensed LPWAN; cheap, operator-independent, but less QoS/coverage guarantees.
- **Wi-Fi, Bluetooth, Zigbee, Thread** — short-range/local IoT.
- Cellular's edge: global coverage, security, SLA, and roaming out of the box.

## The SIM story — where IoT gets interesting

| Form | What it is |
|---|---|
| **Removable SIM** | Classic plastic card — impractical for sealed/remote devices |
| **eSIM (eUICC)** | Soldered chip, **remotely reprogrammable** — change operator over the air (**RSP**: Remote SIM Provisioning) |
| **iSIM (integrated SIM)** | SIM function baked into the device's main chip — smallest, cheapest, lowest power |

**Why it matters**: a device shipped worldwide can be provisioned/re-provisioned to the best local operator without physical access. eSIM management platforms are a real software product space (GSMA SGP standards).

## Connectivity management — the operator/enterprise platform

A business with 100,000 connected devices needs a **Connectivity Management Platform (CMP)** to:
- provision/activate/deactivate SIMs in bulk
- monitor usage and status per device
- enforce policies (e.g. block a hacked tracker sending gigabytes)
- **bill** IoT plans (often pooled data across the fleet — ties to [rating/charging](billing/rating-charging.md))

Examples: Cisco IoT Control Center (ex-Jasper), Ericsson IoT Accelerator, Vodafone GDSP, plus operator IoT portals.

## Security — a huge concern

IoT devices are notoriously weak targets (default passwords, no updates, long lifetimes). Telecom-side defenses:
- **Private APNs / network isolation** — devices can't reach the open internet or each other.
- **Anomaly detection** on traffic (a meter suddenly uploading gigabytes = compromised).
- **eSIM + strong identity** instead of guessable credentials.
- Apply the same principles as [IoT-adjacent web security](../cybersecurity/web-security.md) and [least privilege](../cybersecurity/fundamentals.md).

> The Mirai botnet (2016) weaponized hundreds of thousands of insecure IoT devices — a permanent reminder that connectivity without security is a liability at scale.

## For developers

IoT connectivity exposes APIs: provision a SIM, pull usage, set policies, receive events. Building on top of a CMP is ordinary backend/API work — the telecom complexity is abstracted behind REST. Combine with [edge/5G](5g.md) for low-latency device intelligence.
