# 📡 Telecom

Everything that makes voice, data and messaging travel across the planet — from radio waves to the billing system that charges for them. Written for developers and IT people who need to understand telecom, not just use a phone.

## 🗺️ Map

| Section | What you'll find |
|---|---|
| [fundamentals.md](fundamentals.md) | How mobile networks work; 1G → 5G generations explained |
| [network-architecture.md](network-architecture.md) | RAN, core network, transport — the three layers |
| [5g.md](5g.md) | 5G architecture, network slicing, edge, what's real vs hype |
| [protocols.md](protocols.md) | SS7, Diameter, GTP, SIP, VoLTE — the signaling that runs it all |
| [volte-ims.md](volte-ims.md) | How voice works in the all-IP era (VoLTE/VoNR/VoWiFi, IMS) |
| [roaming-interconnect.md](roaming-interconnect.md) | How networks cooperate & settle money across borders |
| [provisioning-architecture.md](provisioning-architecture.md) | How subscriber activation actually works: connectors, orchestration, idempotency |
| [nokia-instantlink-flowone.md](nokia-instantlink-flowone.md) | A real provisioning platform in detail: flow-based architecture, version migration |
| [core-network-migration.md](core-network-migration.md) | Migrating a live core between vendors (e.g. Nokia → Huawei) without an outage |
| [number-portability.md](number-portability.md) | The cross-operator workflow behind keeping your number when you switch |
| [mobile-number-portability.md](mobile-number-portability.md) | MNP protocol-level detail: NPG message flow, port-in/out, platform migration |
| [oss-bss.md](oss-bss.md) | The software that operators run their business on |
| [billing/](billing/) | Rating, charging, mediation, OCS, revenue assurance |
| [camara-network-apis.md](camara-network-apis.md) | Coding against telecom: SIM-swap, QoD, number verification |
| [private-5g.md](private-5g.md) | Private/enterprise 5G networks — factories, ports, campuses |
| [edge-computing-mec.md](edge-computing-mec.md) | MEC: compute pushed to the radio edge, and what actually runs there |
| [telecom-security.md](telecom-security.md) | Securing the network: SS7 risks, SIM swap, 5G security |
| [operators.md](operators.md) | Key operators worldwide and their footprint |
| [vendors.md](vendors.md) | Ericsson, Nokia, Huawei, Cisco... who builds the gear |
| [iot-m2m.md](iot-m2m.md) | IoT connectivity: NB-IoT, LTE-M, eSIM, 5G massive IoT |
| [glossary.md](glossary.md) | The acronym jungle, decoded |

## 🧭 How to read this section

Telecom is an onion of acronyms. Start with [fundamentals](fundamentals.md) to get the mental model (radio → core → services → billing), then dive where you need. The [glossary](glossary.md) is your safety net — telecom has more three-letter acronyms than any other industry.

## 💡 Why a developer should care

- **APIs are eating telecom**: CAMARA, network-as-code, programmable connectivity.
- **Billing = software**: rating engines, real-time charging, mediation — pure backend systems at massive scale.
- **5G is a cloud-native platform**: the core network is now microservices (SBA). Kubernetes runs telephony.
- **IoT rides telecom**: every connected device needs a network and a SIM profile.
