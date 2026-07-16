# Network Provisioning Architecture

Everything else in this library's telecom module explains *what* a network does. This page explains the part almost nobody documents well: **how a subscriber actually gets activated, changed, or ported** — the systems and connectors that turn a BSS order into a live service on the network. This is production knowledge from working provisioning systems at operator scale, not a whitepaper summary.

## The problem provisioning actually solves

A customer buys a plan on the website. Somewhere between "order confirmed" and "the SIM works," a chain of systems has to:

```
BSS Order ──► Provisioning Orchestrator ──► [connector] ──► HLR/HSS (2G/3G/4G identity)
                                        ──► [connector] ──► UDM (5G identity)
                                        ──► [connector] ──► EPS/VoLTE profile
                                        ──► [connector] ──► PCRF/PCF (policy/quota)
                                        ──► [connector] ──► Billing/BSCS (rating account)
                                        ──► [connector] ──► IN/Prepaid platform (if prepaid)
```
One business action ("activate this line") fans out into **many independent network element updates**, each with its own protocol, its own failure modes, and its own idea of "success." Provisioning orchestration exists to make that fan-out look atomic to the business, even though the underlying systems have no shared transaction.

## The connector pattern — the actual unit of work

In practice, you don't talk to HLR/HSS/UDM/PCRF directly from the orchestrator. You build a **connector** per network element family: a service that translates a generic provisioning command into that element's specific protocol/API and normalizes the response.

```java
public interface NetworkConnector {
    ProvisioningResult activate(SubscriberProfile profile);
    ProvisioningResult modify(SubscriberProfile profile, ChangeSet changes);
    ProvisioningResult suspend(String subscriberId);
    ProvisioningResult reconcileStatus(String subscriberId);   // ask the network what IT thinks is true
}
```

Why this shape, specifically:
- **Vendor isolation**: swap Nokia for Huawei behind the same interface — this is exactly what makes a core migration (below) tractable instead of a full BSS rewrite.
- **Independent failure handling**: HLR times out, PCRF doesn't — you need per-connector retry/circuit-breaker policy, not one blanket timeout for the whole chain (see [resilience patterns](../backend/microservices/spring-microservices.md)).
- **Reconciliation as a first-class operation**, not an afterthought: the network's actual state and what your BSS *thinks* happened will diverge — timeouts where the write actually succeeded, retries that duplicate, manual interventions on the network side. `reconcileStatus` exists because "did it actually work?" is a real, recurring question, not a hypothetical.

## Provisioning workflow orchestration

A real activation isn't one call — it's a sequence with dependencies, partial-failure handling, and (often) human intervention points:

```
1. Validate order (plan exists, SIM available, no duplicate active line)
2. Reserve resources (MSISDN, ICCID pairing)
3. Provision core identity (HLR/HSS or UDM depending on subscriber type)
4. Provision service profile (VoLTE, data bundle, roaming flags)
5. Provision policy (PCRF/PCF — quota, QoS profile)
6. Activate billing account (BSCS or equivalent)
7. Confirm — or compensate on failure
```
This is a textbook fit for a **workflow engine** (BPMN — see [bpmn-workflow-engines](../backend/java-spring/bpmn-workflow-engines.md)) rather than hand-rolled sequential code: each step has its own retry policy, timeout, and — critically — its own **compensating action** if a later step fails. If step 5 fails after step 3 succeeded, you don't leave a half-activated subscriber; you roll back the identity provisioning too. Designing those compensations *is* most of the real engineering work in a provisioning system — the happy path is the easy 20%.

## Idempotency — non-negotiable, for a specific reason

Provisioning requests get retried constantly: a timeout doesn't tell you whether the network element actually applied the change. A **non-idempotent** activate call sent twice can create duplicate profiles, double-count resources, or leave the element in an inconsistent state that only shows up days later as a billing dispute or a subscriber who can't make calls.

```java
// Every provisioning request carries a client-generated correlation ID
ProvisioningRequest request = new ProvisioningRequest(
    correlationId,          // same ID on retry = same logical operation
    subscriberId, changeSet
);
// The connector checks: "have I already applied this correlationId?"
// before doing anything — safe to retry blindly at the orchestrator level.
```
This is the exact same principle as [Kafka consumer idempotency](../data-engineering/streaming-kafka.md) and [mediation dedup](billing/mediation.md) — provisioning, mediation, and billing all converge on "the network doesn't guarantee exactly-once, so your application layer has to."

## Legacy protocol reality (what you actually integrate with)

Modern architecture diagrams show clean REST/HTTP2 [5G service-based interfaces](network-architecture.md). Real-world provisioning still routinely talks to:
- **CORBA / proprietary TCP protocols** for older HLR/BSCS interfaces
- **SOAP/XML web services** (TIBCO-mediated in many deployments) for OSS-to-network integration
- **File-based batch interfaces** for bulk provisioning (mass SIM activation campaigns)
- **LDAP-style directory protocols** for some HSS implementations

A connector layer earns its keep precisely because it hides this zoo behind one interface — the orchestrator and the business logic should never need to know whether the element underneath speaks SOAP from 2008 or HTTP/2 from this year.

## Where this connects

[Core network migration](core-network-migration.md) is what happens when you need to swap the vendor behind these connectors without breaking provisioning for millions of live subscribers. [Number portability](number-portability.md) is a specialized, regulator-mandated provisioning workflow with its own protocol (the NP process). [Revenue assurance](billing/revenue-assurance.md) exists partly *because* provisioning and billing can silently diverge — a service can be live on the network with no matching billing record, or vice versa.
