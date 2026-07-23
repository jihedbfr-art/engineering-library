# BPMN Workflow Engines — Camunda

When a business process has multiple steps, conditional branches, timeouts, human approval points, and needs to survive a service restart mid-process — hardcoding it as sequential Java code stops working. This is what workflow engines like **Camunda** solve, using **BPMN** (Business Process Model and Notation) as the actual executable definition, not just documentation.

## Why model the process instead of coding it

```
Hardcoded version:                    BPMN version:
if (orderValid) {                     A visual diagram IS the executable process.
  chargePayment();                    Each step is independently retryable,
  if (paymentOk) {                    the current state of any in-flight process
    reserveStock();                   instance is queryable, and a business analyst
    ...                               can read (and sometimes edit) the flow without
  } else { compensate(); }            reading Java.
}
```
The real win isn't "no code" — you still write Java for the actual logic (**service tasks**). The win is that **orchestration, state, retries, and visibility become the engine's job**, not something you reinvent per process.

## Anatomy of a BPMN process

```
[Start Event] → [Service Task: Validate Order] → [Gateway: Valid?]
                                                       ├─Yes→ [Service Task: Charge Payment]
                                                       │           → [Gateway: Success?]
                                                       │              ├─Yes→ [Service Task: Ship] → [End]
                                                       │              └─No → [Service Task: Compensate] → [End]
                                                       └─No → [End Event: Rejected]
```

| Element | Meaning |
|---|---|
| **Service Task** | Calls actual code (a Java delegate, a REST call, a message) |
| **User Task** | Waits for a human to act (an approval step) — the process genuinely pauses here, possibly for days |
| **Gateway** | A decision point (exclusive = one path, parallel = all paths, inclusive = conditional multiple) |
| **Boundary Timer Event** | "If this step hasn't completed in X time, do Y" — escalation without extra code |
| **Message Event** | The process waits for an external signal (e.g. a Kafka event, a callback) before continuing |

## A service task, in code

```java
@Component("chargePaymentDelegate")
public class ChargePaymentDelegate implements JavaDelegate {
    @Autowired private PaymentClient paymentClient;

    @Override
    public void execute(DelegateExecution execution) {
        String orderId = (String) execution.getVariable("orderId");
        PaymentResult result = paymentClient.charge(orderId);
        execution.setVariable("paymentSuccess", result.isSuccess());
        if (!result.isSuccess()) {
            throw new BpmnError("PAYMENT_FAILED", result.getErrorMessage());
            // caught by an error boundary event → routes to a compensation path
        }
    }
}
```
The BPMN diagram references `chargePaymentDelegate` by name; Camunda calls this Spring bean at that step. Business logic stays in ordinary, testable Java — the engine only owns sequencing, state, and error routing.

## DMN — decision tables as data, not nested if/else

```
┌──────────────┬───────────────┬──────────────┐
│ orderAmount  │ customerTier  │ approvalNeeded│
├──────────────┼───────────────┼──────────────┤
│ < 1000       │ any           │ false         │
│ >= 1000      │ "gold"        │ false         │
│ >= 1000      │ any           │ true          │
└──────────────┴───────────────┴──────────────┘
```
**DMN (Decision Model and Notation)** externalizes business rules like this into a table the process consults — when a business analyst wants to change the approval threshold, they edit the table, not a Java conditional buried three services deep. Wiring: a Business Rule Task in the BPMN process evaluates the DMN table and reads the result into a process variable, same as any service task's output.

## Why this fits provisioning-style workflows specifically

If you've read [provisioning-architecture](../../telecom/provisioning-architecture.md), the fit should be obvious: multi-step network provisioning with per-step compensation is *exactly* the shape BPMN was built for — a service task per network element connector, boundary error events routing to compensating tasks, a timer event escalating a stuck HLR update after N minutes. Modeling a provisioning workflow as hardcoded sequential Java is precisely the pattern that becomes unmaintainable once you have 6+ steps with real failure/compensation logic at each one.

## Operational advantages that matter in production

- **Process instances survive restarts** — state is persisted (typically to a relational DB), so a server crash mid-process doesn't lose in-flight work; the engine resumes exactly where it left off.
- **Visibility into stuck processes** — Camunda's Cockpit UI shows exactly which instances are stuck at which step, for how long, with what variables — versus grepping logs to reconstruct "where did this order get stuck" across services.
- **Versioning** — deploy a new process definition without breaking already-running instances of the old version; they finish on the version they started on.

## When NOT to reach for a workflow engine

- Simple, short, single-service operations — the orchestration overhead isn't worth it for a two-step operation with no human wait and no compensation logic.
- Extremely high-throughput, sub-millisecond paths — a persisted-state engine adds latency a hot path (like [OCS charging](../../telecom/billing/ocs.md)) generally can't afford; that stays hand-optimized code.
- A team with zero appetite for maintaining BPMN diagrams as living artifacts — an out-of-date, ignored diagram is worse than honest code, because it actively lies about what the process does.
