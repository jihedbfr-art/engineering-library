# Nokia InstantLink & FlowOne — Provisioning Platform Deep Dive

[provisioning-architecture](provisioning-architecture.md) covers the general connector pattern. This page is about one specific, real, widely-deployed provisioning platform family — Nokia's **InstantLink** (and its evolution into **FlowOne**) — because "provisioning system" stays abstract until you've actually worked inside one of these.

## What InstantLink actually is

InstantLink is a **provisioning orchestration and mediation platform**: it sits between the BSS/OSS layer (order management, CRM) and the network elements (HLR, HSS, VLR, IN/prepaid platforms, billing systems), and its entire job is turning a business-level order into the sequence of network-element-specific commands that actually activate/modify/suspend a subscriber. Conceptually it's an implementation of exactly the [connector + orchestration pattern](provisioning-architecture.md) described generically elsewhere in this module — InstantLink is what that pattern looks like when a specific vendor builds and sells it as a product, with years of accumulated flow logic for real-world telecom scenarios.

## Flow-based architecture — the core design idea

InstantLink models each provisioning scenario as a **flow**: an explicit, ordered sequence of steps against one or more network elements, each step with its own retry/timeout/error-handling behavior.

```
Flow: "Activate Prepaid Subscriber"
  Step 1: Validate subscriber data (MSISDN available, plan valid)
  Step 2: Create subscriber on HLR
  Step 3: Create subscriber on IN/prepaid platform
  Step 4: Activate on billing (BSCS or equivalent)
  Step 5: Confirm — or trigger the flow's defined rollback steps
```
This should look familiar — it's the same shape as a [BPMN business process](../backend/java-spring/bpmn-workflow-engines.md), except InstantLink's flows are configured through its own proprietary flow-definition tooling rather than an open standard like BPMN. That difference — proprietary flow engine vs. open workflow standard — is exactly the kind of thing that makes a [core migration away from this platform](core-network-migration.md) a genuine re-design rather than a like-for-like swap: you can't export an InstantLink flow into Camunda, you have to understand what the flow actually accomplishes and rebuild that logic on the new platform.

## Version evolution: InstantLink 8 → 19 → FlowOne

Real migration projects inside this same product family happen too, independent of switching vendors entirely:

```
InstantLink 8 (older)  →  InstantLink 19  →  FlowOne (current generation)
```
Each major version upgrade is itself a scoped migration project — new flow-definition tooling, sometimes new integration protocols, and a requirement to verify that every existing flow still behaves identically after the upgrade. **The same discipline as any other legacy migration applies here**: enumerate the existing flows and their edge cases before assuming the new version's equivalent flow behaves the same way, test in DEV/UAT with production-representative data, stage the cutover, don't trust the vendor's migration notes alone to have captured every operator-specific customization made over the years.

**FlowOne** represents Nokia's move toward a more modern, more standards-aligned provisioning platform — the direction of travel across the industry generally (see [oss-bss](oss-bss.md) on TM Forum Open APIs and the broader shift toward standardized, less proprietary integration). The practical reality of adopting it in an existing deployment is the same as any platform upgrade: real requirement-archaeology work, not a checkbox.

## Connector development against InstantLink — what the actual work looks like

Building or maintaining a network element connector in this ecosystem typically means J2EE development against Nokia's APIs/SDKs, implementing the specific protocol dialect each network element expects (often SOAP/XML web services, sometimes proprietary TCP protocols, mediated through middleware like TIBCO for the broader integration layer):

```java
// Illustrative shape of what a real InstantLink-integrated connector looks like —
// vendor SDK specifics vary by InstantLink version and target network element
public class HlrProvisioningConnector implements NetworkConnector {
    private final InstantLinkFlowClient flowClient;   // vendor SDK / API client

    public ProvisioningResult activate(SubscriberProfile profile) {
        FlowRequest request = FlowRequest.builder()
            .flowName("ActivateSubscriberHLR")
            .parameter("msisdn", profile.getMsisdn())
            .parameter("imsi", profile.getImsi())
            .correlationId(profile.getCorrelationId())   // idempotency — see provisioning-architecture.md
            .build();
        FlowResponse response = flowClient.execute(request);
        return ProvisioningResult.from(response);
    }
}
```
The engineering reality: a large share of the real work is **reading and reverse-engineering existing flow definitions** to understand exactly what an operator-specific flow does today — including undocumented parameters and quiet exception handling added over years for specific business scenarios — before writing a single line of new connector code. This is the InstantLink-specific instance of the [requirement archaeology](../legacy-modernization/legacy-migration-playbook.md) principle: the platform encodes years of accumulated operator-specific logic that isn't fully captured anywhere except in the flow definitions and the people who maintained them.

## Where InstantLink/FlowOne sits in the bigger picture

```
BSS Order Management ──► InstantLink/FlowOne (orchestration + flow execution)
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
                  HLR/HSS      IN/Prepaid       Billing (BSCS)
```
It's the concrete, productized realization of the abstract "provisioning orchestrator + connectors" architecture — useful to know by name specifically because it (and platforms like it — Comptel's provisioning suite is another common one in this space) is what a large share of real-world operator provisioning actually runs on, not a hypothetical reference architecture.

## Where this connects

[Core network migration](core-network-migration.md) is what happens when the network elements behind these flows change vendor (Nokia → Huawei) — the InstantLink/FlowOne flows themselves have to be rebuilt against the new elements' protocols, one flow at a time, following the same batch-and-validate discipline. [Mobile number portability](mobile-number-portability.md) is a specialized flow category within this same platform family — porting-in and porting-out are themselves provisioning flows, just triggered by a cross-operator event instead of a BSS order.
