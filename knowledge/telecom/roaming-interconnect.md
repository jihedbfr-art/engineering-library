# Roaming & Interconnection

How networks cooperate so your phone works abroad and calls cross between operators. This is where telecom becomes a **global federation of businesses** settling money with each other.

## Interconnection — networks talking to each other

No operator reaches every phone on Earth, so they interconnect. When you call someone on a different network, your operator hands the call off and **pays a termination fee** to the receiving operator.

```
Caller (Operator A) ──call──► Interconnect ──► Operator B ──► Callee
                                   │
                        A pays B a "termination rate"
```

- **Termination rate**: price to terminate a call/SMS on another network. Often regulated (mobile termination rates, MTRs) to prevent abuse.
- **Bill & keep** vs **calling-party-pays**: different regulatory models for who pays.
- **Transit carriers**: wholesale operators that route traffic between networks that don't connect directly (the "tier-1" of voice).

## Roaming — your SIM on a foreign network

When you travel, your phone attaches to a **visited network** (VPMN) but your identity and billing still belong to your **home network** (HPMN).

```
You abroad ──► Visited Network (VPMN) ──signaling──► Home Network (HPMN)
                    │                                      │
              provides radio + service          authenticates you,
              (records usage)                    owns the subscription
                    └──────── settlement ────────────────┘
              (visited bills home; home bills you)
```

### The roaming flow

1. Phone finds the visited network, sends your **IMSI**.
2. Visited network asks your **home network** (via signaling) "is this subscriber valid? what can they do?"
3. Home network authenticates you and returns your profile.
4. You get service; the visited network **records your usage (TAP/CDR)**.
5. Usage records are exchanged and **settled** between operators.
6. Your home operator bills you (often with a roaming markup, or "roam-like-home" in regulated zones like the EU).

### The signaling behind it

- **2G/3G**: SS7 MAP ([protocols](protocols.md)) — also the source of roaming-fraud and interception risks.
- **4G**: Diameter (S6a/S6d interfaces) via **IPX** networks.
- **5G**: service-based, HTTP/2 with security edge protection (SEPP).

### Settlement & data exchange

- **TAP** (Transferred Account Procedure): the standardized file format operators exchange to bill each other for roaming usage.
- **Data Clearing Houses (DCH)** and **Financial Clearing Houses (FCH)**: third parties that process TAP files and net out payments between hundreds of operators.
- **IPX (IP eXchange)**: the private, quality-managed IP backbone operators use to interconnect (instead of the public internet) — run by carriers like BICS, Syniverse, Tata, Orange International.

## Roaming fraud — a real cost

Because usage records arrive *after* the fact, roaming is a fraud hotspot:
- A fraudster racks up huge premium/roaming charges abroad before records reach the home network.
- **NRTRDE** (Near Real-Time Roaming Data Exchange) was introduced to shorten that window (records within ~4 hours).
- Ties to [revenue assurance & fraud](billing/revenue-assurance.md) and real-time [OCS](billing/ocs.md) controls.

## Regulation shapes everything

- **EU "Roam Like At Home"**: abolished retail roaming surcharges within the EU — a regulatory, not technical, change with huge market impact.
- **Mobile Termination Rates**: regulators cap them to keep cross-network calls affordable.
- Understanding telecom means understanding that **regulators are a first-class actor**, not a footnote.

## For engineers

Roaming/interconnect is a **B2B settlement and data-exchange system**: standardized file formats (TAP), clearing intermediaries, reconciliation, dispute handling, fraud detection — classic large-scale data engineering with money and multi-party trust. The federation model rhymes with [inter-service auth](../cybersecurity/web-security.md) and distributed systems.
