# Private 5G & Enterprise Networks

A **private mobile network** is a 5G (or 4G/LTE) network dedicated to one organization — a factory, port, mine, hospital, campus — instead of the public operator network. One of telecom's fastest-growing enterprise opportunities.

## Why an enterprise wants its own network

| Need | Why public networks fall short |
|---|---|
| **Coverage** | Deep indoor / remote sites (factory floor, underground mine) with no good public signal |
| **Control** | Guaranteed capacity and priority for critical operations |
| **Latency & reliability** | URLLC for robots/automation — can't depend on a shared network |
| **Data sovereignty** | Sensitive data never leaves the premises |
| **Customization** | Slicing/QoS tuned to the specific use case |

## Wi-Fi vs Private 5G (the honest comparison)

| | Wi-Fi | Private 5G |
|---|---|---|
| Cost | Low | Higher |
| Mobility/handover | Weak across large areas | Seamless |
| Coverage per AP | Small | Large |
| Deterministic latency | Hard | Yes (URLLC) |
| Device density | Limited | Massive |
| Security model | Per-AP | SIM-based, network-wide |

Private 5G wins for **large, mobile, mission-critical, dense** environments; Wi-Fi still wins on cost for offices.

## Spectrum options

The classic blocker (spectrum is licensed) is being solved by dedicated enterprise spectrum:
- **Shared/local licensed bands**: e.g. **CBRS** in the US, local 5G bands in Germany/Japan/UK — enterprises can get spectrum directly.
- **Operator-provided**: the carrier carves out a slice of its licensed spectrum.

## Deployment models

1. **Fully private (standalone)**: all equipment (RAN + core) on-premises, enterprise-run. Max control & sovereignty.
2. **Hybrid**: local RAN + on-prem edge, but some functions or management from the operator/cloud.
3. **Network slicing on public 5G**: a logical private network carved from the operator's network ([slicing](5g.md)) — less isolation, faster to deploy.

## Reference use cases

- **Manufacturing (Industry 4.0)**: AGVs/robots, machine vision QC, predictive maintenance sensors.
- **Ports & logistics**: automated cranes, remote-controlled vehicles.
- **Mining & energy**: autonomous haulage, remote operations in hostile environments.
- **Healthcare**: reliable connectivity for devices, in-hospital asset tracking.
- **Stadiums/campuses**: dense, high-quality coverage for thousands.

## The stack (what you deploy)

```
Enterprise devices/sensors ──► on-prem RAN (small cells) ──► on-prem/edge 5G core (UPF local)
                                                                   │
                                                    Edge compute (MEC) runs the apps locally
                                                    Management/orchestration (often cloud)
```
The **UPF stays local** so data and low latency never leave the site — the CUPS/edge idea from [network-architecture](network-architecture.md) made concrete.

## For engineers & IT

Private 5G blurs into IT/DevOps:
- the 5G core runs as **containers on Kubernetes** at the edge ([k8s](../devsecops/containers/kubernetes-essentials.md))
- integration with enterprise IT, identity, and security policies
- observability, automation and lifecycle management like any cloud-native platform ([observability](../devsecops/monitoring/observability.md))
- vendors: operators, plus Nokia, Ericsson, and cloud players (AWS Private 5G, Azure Private MEC) packaging it as a managed product.

The takeaway: "telecom network" is becoming "a Kubernetes platform with radios" — squarely in reach of software and infrastructure engineers.
