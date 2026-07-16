# OSS / BSS — Running the Telecom Business

Two software worlds every operator lives on. Rule of thumb:
- **OSS = the network** (build it, run it, keep it healthy) — *Operations Support Systems*
- **BSS = the customer & money** (sell it, bill it, support it) — *Business Support Systems*

```
        Customer ──► BSS (CRM, ordering, billing, care) ──► OSS (provisioning,
                                                              assurance, inventory) ──► Network
```

## BSS — the customer-facing stack

| Domain | Job |
|---|---|
| **CRM** | Customer records, interactions, campaigns |
| **Product catalog** | Plans, bundles, offers — the source of truth for what's sellable |
| **Order management** | Turns "I want plan X" into orchestrated provisioning steps |
| **Billing & charging** | Rates usage, produces invoices, takes payment → [billing/](billing/) |
| **Customer care** | Support tooling, self-service apps, dispute handling |
| **Partner/settlement** | Interconnect and roaming settlement between operators |

## OSS — the network-facing stack

| Domain | Job |
|---|---|
| **Inventory** | What equipment/resources exist and how they connect |
| **Provisioning / activation** | Turn services on/off in the network (activate a SIM, a data plan) |
| **Service assurance** | Monitor health, detect faults, correlate alarms |
| **Performance management** | KPIs, capacity planning, SLA tracking |
| **Network orchestration** | Automate configuration (increasingly NFV/SDN, cloud-native) |

## The frameworks you'll hear about

- **TM Forum eTOM** — the standard process map for telecom operations (a reference taxonomy of every business process).
- **TM Forum Open APIs** — standardized REST APIs between BSS/OSS systems (so vendors interoperate).
- **ODA (Open Digital Architecture)** — TM Forum's blueprint for cloud-native, component-based BSS/OSS.
- **MEF** — standards for carrier Ethernet and inter-provider services.

## NFV & SDN — why OSS is becoming software engineering

- **NFV (Network Function Virtualization)**: run network functions (firewalls, routers, even the 5G core) as software on standard servers, not dedicated boxes.
- **SDN (Software-Defined Networking)**: separate the control decisions from the forwarding hardware; program the network centrally.
- Result: the network is provisioned like cloud infra — think [Terraform for telephony](../devsecops/iac/terraform-basics.md). Orchestrators (ONAP, vendor MANO) drive it.

## The developer era: network-as-code (CAMARA / Open Gateway)

The industry is exposing network capabilities as **developer APIs**:

| API | What it does |
|---|---|
| **SIM Swap** | Tells you if a number's SIM changed recently → anti-fraud signal |
| **Number Verification** | Confirms the user controls the phone number (silent auth) |
| **Device Location** | Verifies a device is where it claims (fraud, compliance) |
| **Quality on Demand** | App requests a low-latency/high-bandwidth boost for a session |
| **Device Status** | Reachability, roaming state |

- Standardized by **CAMARA** (Linux Foundation) + **GSMA Open Gateway**.
- Business impact: banks use SIM-Swap + Number-Verification to fight account-takeover fraud — a direct tie to [authentication security](../cybersecurity/web-security.md).
- For you: telecom finally has clean REST APIs. No SS7 knowledge required to consume them.

## Revenue assurance & fraud (where BSS meets security)

Operators lose real money to fraud (SIM boxes, subscription fraud, interconnect bypass) and billing leakage. **Revenue Assurance** and **Fraud Management** systems reconcile network usage against what got billed — a data-engineering problem at massive scale. See [billing/revenue-assurance](billing/revenue-assurance.md).
