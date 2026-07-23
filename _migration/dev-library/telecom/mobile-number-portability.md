# Mobile Number Portability (MNP) — Protocol & Systems Deep Dive

[number-portability.md](number-portability.md) covers the general cross-operator workflow. This page goes deeper into **mobile-specific** portability: the actual message flow, the NPG's role in detail, and the systems-integration reality of running or migrating an MNP platform (drawn from real NPG development and a NUMLEX-style portability migration).

## The two porting directions — and why they're not symmetric

```
PORT-IN:  a subscriber is joining YOUR network from another operator
          → your NPG receives/validates the request, your provisioning
            activates the subscriber, at the coordinated cutover time

PORT-OUT: a subscriber is LEAVING your network for another operator
          → your NPG validates the request (matching account details,
            checking eligibility — e.g. no unresolved contract obligation),
            confirms or rejects, deactivates at cutover
```
Port-out validation is where an operator has genuine, legitimate business logic to enforce (contract terms, outstanding balance rules under local regulation) — it's also, in every market with MNP regulation, tightly bounded by rules preventing operators from using validation as a de facto retention tactic. The NPG's validation logic has to reflect exactly what's regulator-approved, not what's commercially convenient — get this wrong and it's a compliance finding, not just a bug.

## The message flow, protocol-level

Real MNP systems exchange structured messages (historically often SOAP/XML, increasingly REST in modernized platforms) between the donor NPG, the recipient NPG, and the central/national portability database or clearinghouse:

```
Recipient NPG                Central DB/Clearinghouse            Donor NPG
     │                                                                │
     │──PortRequest(msisdn, subscriberDetails)────────►│                │
     │                                                    │──Validate──►│
     │                                                    │◄─Confirm/Reject
     │◄────────PortResponse(status, cutoverWindow)───────│                │
     │                                                                │
     │  [at scheduled cutover time]                                  │
     │──ActivatePort(msisdn)──────────────────────────►│                │
     │                                                    │──Deactivate─►│
     │◄────────PortComplete───────────────────────────│                │
     │                                                                │
     │──[trigger internal provisioning: activate on HLR/UDM]         │
     │──[trigger billing: start billing this subscriber]             │
```
The NPG's core job is translating between this **external, standardized-by-regulation protocol** and the operator's **internal provisioning/billing systems** ([provisioning-architecture](provisioning-architecture.md)) — it's a connector in exactly the same architectural sense, just facing a competitor's system and a regulator instead of an internal network element.

## Why the NPG has to be resilient in ways internal systems don't

An internal provisioning connector talks to systems your own team controls. The NPG talks to **every other operator in the market**, each running their own NPG implementation, of varying quality and reliability — you cannot assume the donor operator's system responds promptly, correctly, or even consistently across every request. Real NPG implementations need:

- **Aggressive but sane timeout/retry policy** on cross-operator messages — a slow or unresponsive donor operator can't be allowed to hang your own porting queue indefinitely.
- **Explicit state persistence for every in-flight port** — if a message is lost or a system restarts mid-port, the process has to resume from its actual last-known state, not silently restart or silently drop, since a dropped port request affects a real subscriber's live service.
- **Audit logging that satisfies regulatory reporting** — porting SLAs are usually regulator-mandated and regularly audited; "we don't have a clean record of when this port completed" is a compliance problem, not just an operational inconvenience.

## Migrating an NPG platform (the NUMLEX-style scenario)

When a market-wide portability system itself gets replaced or upgraded (a new central clearinghouse platform, a new NPG product), every operator's NPG has to migrate its integration in a coordinated, market-wide cutover — arguably harder than a single-operator core migration, because **no operator controls the timeline alone**; it's set by the regulator/clearinghouse and every operator in the market has to be ready simultaneously.

```
1. New central platform/protocol specification published by regulator/clearinghouse
2. Every operator develops/updates their NPG integration against the new spec
3. Market-wide DEV/UAT testing window — often coordinated multi-operator test cycles,
   since a port only succeeds if BOTH sides' NPGs handle the new protocol correctly
4. Inspection and validation of existing NPG code against the new solution's
   conformance requirements — verifying every currently-handled scenario
   (successful ports, rejections, edge cases) still works correctly
5. Market-wide cutover, often on a regulator-mandated date — no gradual,
   operator-by-operator rollout is possible the way an internal migration allows,
   because porting is inherently a two-party (or clearinghouse-mediated) protocol
```
The practical work at the operator level looks like: **inspecting and adapting the existing NPG application's source code** against the new solution's requirements, verifying the currently-implemented business flows remain conformant, and coordinating closely with the platform vendor/regulator on the shared cutover date — a genuinely different risk profile from an internal migration, since your own readiness alone doesn't determine success; every other operator's readiness does too.

## Fixed-line portability — a brief, honest note on what differs

Fixed-line number portability shares the "central registry + routing lookup" structure but adds a real-world constraint mobile porting doesn't have: **the receiving operator has to actually be able to serve that physical location** — the number is tied to a line, and a line is tied to physical infrastructure that may or may not reach that address. Validation logic for fixed porting has to check serviceability, not just account/contract eligibility — a materially different (and often more complex) rejection-reason space than mobile porting.

## Where this connects

This page is the protocol-level detail underneath [number-portability.md](number-portability.md)'s general workflow, and a direct application of [provisioning-architecture](provisioning-architecture.md)'s connector/idempotency principles to a specifically adversarial-trust context (a competitor's system, not an internal one). The NPG migration scenario above follows the exact same discipline as [legacy-migration-playbook](../legacy-modernization/legacy-migration-playbook.md) — requirement archaeology on the existing NPG's real behavior, staged validation, coordinated cutover — with the added wrinkle that readiness isn't fully within any single operator's control.
