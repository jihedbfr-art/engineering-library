# Per-tenant budget caps — cutting off one customer's spend without affecting everyone else

[`model-routing-and-cost-control.md`](model-routing-and-cost-control.md)'s `CostTracker` answers
"how much have we spent." It doesn't answer "has *tenant X* spent more than they're allowed to,"
and those are different questions the moment more than one paying customer shares the same model
deployment. A single global cost counter can tell you the whole system is over budget after the
fact; it can't stop one noisy or compromised tenant from burning through everyone else's
allocation before anyone notices.

## Why this needs a tenant identity, not just a bigger CostTracker

Wrapping the existing tracker in a per-tenant dictionary is the easy 80%: bucket recorded calls by
`tenant_id`, sum, compare to a cap. The genuinely missing 20% is *where that `tenant_id` reliably
comes from* — it has to be attached to the request before the router or the cache ever sees it,
not inferred after the fact from which API key made the call, because by the time you're
inferring it you've already spent the tokens you were trying to cap.

This is the same shape of problem [`11-agent-identity-and-access`](../11-agent-identity-and-access/)
solves for *agents*: a per-agent OAuth2 client makes "which agent is this" an answerable, enforced
question instead of a shared-key guess. A tenant-scoped claim on the same token — or a separate
tenant-identifying header set by whatever gateway sits in front of this router — does the same job
for tenants. This module doesn't implement that identity layer; it assumes a `tenant_id` string is
already present on the request by the time `route_and_track()` is called, the same way the router
already assumes a request has text to classify.

## What this module ships

[`tenant_budget.py`](tenant_budget.py) is a `TenantBudgetTracker` built directly on the existing
`CostTracker` — one `CostTracker` per tenant rather than a parallel accounting system, so tenant
totals and the system-wide total never disagree by construction. Before a call is routed, `charge()`
checks the tenant's running total against their cap and raises `BudgetExceededError` if the *next*
call would exceed it — checked before the call, not after, because after is a bill you can't get
back.

```python
@dataclass
class TenantBudgetTracker:
    caps: dict[str, float]                     # tenant_id -> monthly cap in the tracker's cost unit
    trackers: dict[str, CostTracker] = field(default_factory=dict)

    def _tracker_for(self, tenant_id: str) -> CostTracker:
        return self.trackers.setdefault(tenant_id, CostTracker())

    def remaining_budget(self, tenant_id: str) -> float:
        cap = self.caps.get(tenant_id)
        if cap is None:
            raise UnknownTenantError(tenant_id)  # fail closed: no cap on file means no spend allowed
        return cap - self._tracker_for(tenant_id).total_cost()

    def charge(self, tenant_id: str, tier: ModelTier, input_tokens: int, output_tokens: int) -> float:
        projected = (
            input_tokens / 1000 * tier.cost_per_1k_input_tokens
            + output_tokens / 1000 * tier.cost_per_1k_output_tokens
        )
        if projected > self.remaining_budget(tenant_id):
            raise BudgetExceededError(tenant_id, projected, self.remaining_budget(tenant_id))
        return self._tracker_for(tenant_id).record(tier, input_tokens, output_tokens)
```

`UnknownTenantError` on a missing cap is the deliberate choice here, not an oversight — the same
fail-closed instinct as the guard chain in
[`08-guards-safety`](../08-guards-safety/safety-and-guardrails.md) and the token validator in
[`11-agent-identity-and-access`](../11-agent-identity-and-access/agent-identity-pattern.md). A
tenant with no budget on file getting unlimited spend by default is a worse failure mode than a
legitimate tenant getting a hard error that surfaces a provisioning bug immediately.

## What happens when a tenant hits the cap

This module raises; it doesn't decide the product behavior. Three real options once
`BudgetExceededError` fires, and the right one depends on the product, not the routing layer:
downgrade every further request from that tenant to the cheapest tier instead of blocking outright
(graceful degradation, appropriate for a non-critical feature), block and return a clear
over-budget error the caller can show the user (appropriate for anything metered per-seat), or page
someone if the tenant is internal and the cap getting hit means a bug rather than legitimate usage.
Baking one of these into the tracker itself would make it wrong for two of the three cases.

## Where this sits relative to everything else

| Layer | Question it answers | Module |
|---|---|---|
| Per-tenant budget (this file) | Has *this specific tenant* spent more than they're allowed to? | `10-model-routing-and-cost` |
| Cost tracking | What has the whole system spent, broken down by tier? | [`model-routing-and-cost-control.md`](model-routing-and-cost-control.md) |
| Agent identity | Is this caller who it claims to be? | [`11-agent-identity-and-access`](../11-agent-identity-and-access/agent-identity-pattern.md) |

Budget enforcement is only as trustworthy as the tenant identity feeding it — this module is
downstream of whatever answers "which tenant is this," not a replacement for it.
