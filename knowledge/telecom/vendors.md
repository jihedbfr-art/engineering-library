# Telecom Vendors — Who Builds the Gear & Software

Operators run networks; **vendors** build the equipment and software. Knowing the landscape helps you place any telecom product or job in context.

## Network infrastructure (RAN + Core) — the "big" vendors

| Vendor | Home | Strength |
|---|---|---|
| **Ericsson** | Sweden | RAN leader, 5G, global scale |
| **Nokia** | Finland | RAN + core + fixed + optical; broad portfolio |
| **Huawei** | China | Full stack, cost-competitive; restricted in some Western markets on security grounds |
| **ZTE** | China | Similar full-stack, cost-focused; similar restrictions |
| **Samsung Networks** | South Korea | Rising in 5G RAN (notably US deployments) |

These five supply most of the world's radio and core equipment.

## Open RAN challengers

Open RAN's open interfaces let newer/software players in:
- **Mavenir**, **Parallel Wireless**, **Altiostar** (now Rakuten Symphony)
- **Rakuten Symphony** — Rakuten built a fully cloud-native/Open-RAN network in Japan and now sells the blueprint
- Hyperscalers (**AWS, Azure, Google**) increasingly host telco workloads and edge

## Core, IP & optical

| Vendor | Focus |
|---|---|
| **Cisco** | IP routing/switching, security, some mobile core |
| **Juniper** | IP/MPLS routing (now part of HPE) |
| **Ciena** | Optical transport |
| **Fortinet / Palo Alto** | Telecom security, signaling & GTP firewalls |

## BSS / OSS & billing software

The business/software side ([OSS/BSS](oss-bss.md)):

| Vendor | Known for |
|---|---|
| **Amdocs** | Billing, BSS, customer experience — a telecom software heavyweight |
| **Netcracker** (NEC) | BSS/OSS, digital transformation |
| **CSG** | Revenue management, billing |
| **Ericsson** | Also a major charging/BSS vendor (Ericsson Charging, ex-DigitalRoute mediation) |
| **Nokia** | Nuage (SDN), NetGuard (security), analytics |
| **Optiva, MATRIXX** | Cloud-native convergent charging / real-time [OCS](billing/ocs.md) |
| **Comarch** | BSS/OSS, especially for mid-tier operators |

## Test, assurance & analytics

- **Spirent, Keysight (Ixia), VIAVI** — network testing and validation.
- **NetScout, Empirix** — service assurance and monitoring.
- **Subex, WeDo (Mobileum)** — [revenue assurance & fraud](billing/revenue-assurance.md).

## The shift that matters

Telecom is moving **from appliances to software**:
- Network functions become **containers on Kubernetes** (NFV, cloud-native 5G core).
- Vendors ship software/licenses instead of proprietary boxes.
- Hyperscalers host telco workloads; DevOps/SRE practices enter the network.
- **Consequence for you**: telecom increasingly hires software, cloud, data and security engineers — not only RF specialists. The [DevSecOps](../devsecops/README.md), [Kubernetes](../devsecops/containers/kubernetes-essentials.md) and [observability](../devsecops/monitoring/observability.md) skills in this library apply directly.
