"""Per-tenant budget enforcement on top of CostTracker — the lab for
per-tenant-budget-caps.md. Assumes a tenant_id is already attached to the
request by the time charge() is called; this module doesn't solve where that
identity comes from (see 11-agent-identity-and-access for the analogous
per-agent-identity problem).

Run:  python tenant_budget.py
"""

from __future__ import annotations

import sys
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import get_logger  # noqa: E402

sys.path.insert(0, str(Path(__file__).resolve().parent))
from model_router import TIER_CHEAP, TIER_STRONG, CostTracker, ModelTier  # noqa: E402

logger = get_logger(__name__)


class UnknownTenantError(Exception):
    """Raised when a tenant with no budget cap on file tries to spend. Fail
    closed on purpose — an unprovisioned tenant getting unlimited spend by
    default is worse than a provisioning bug surfacing immediately as an error.
    """

    def __init__(self, tenant_id: str):
        super().__init__(f"no budget cap on file for tenant '{tenant_id}'")
        self.tenant_id = tenant_id


class BudgetExceededError(Exception):
    def __init__(self, tenant_id: str, projected_cost: float, remaining: float):
        super().__init__(
            f"tenant '{tenant_id}' would exceed its budget: call costs "
            f"~{projected_cost:.4f}, only {remaining:.4f} remaining"
        )
        self.tenant_id = tenant_id
        self.projected_cost = projected_cost
        self.remaining = remaining


@dataclass
class TenantBudgetTracker:
    caps: dict[str, float]  # tenant_id -> budget cap, same unit CostTracker reports in
    trackers: dict[str, CostTracker] = field(default_factory=dict)

    def _tracker_for(self, tenant_id: str) -> CostTracker:
        return self.trackers.setdefault(tenant_id, CostTracker())

    def remaining_budget(self, tenant_id: str) -> float:
        cap = self.caps.get(tenant_id)
        if cap is None:
            raise UnknownTenantError(tenant_id)
        return cap - self._tracker_for(tenant_id).total_cost()

    def charge(
        self, tenant_id: str, tier: ModelTier, input_tokens: int, output_tokens: int
    ) -> float:
        """Check the projected cost against the tenant's remaining budget
        *before* recording it — raises instead of recording if it would push
        the tenant over their cap, so the caller can decide what to do (block,
        downgrade tier, page someone) without having already spent the tokens.
        """
        projected = (
            input_tokens / 1000 * tier.cost_per_1k_input_tokens
            + output_tokens / 1000 * tier.cost_per_1k_output_tokens
        )
        remaining = self.remaining_budget(tenant_id)
        if projected > remaining:
            logger.warning(
                "tenant '%s' blocked: call would cost %.4f, only %.4f remaining",
                tenant_id, projected, remaining,
            )
            raise BudgetExceededError(tenant_id, projected, remaining)
        return self._tracker_for(tenant_id).record(tier, input_tokens, output_tokens)

    def spend_by_tenant(self) -> dict[str, float]:
        return {tenant_id: tracker.total_cost() for tenant_id, tracker in self.trackers.items()}


def _demo() -> None:
    budgets = TenantBudgetTracker(caps={"acme-corp": 0.05, "small-customer": 0.01})

    for _ in range(5):
        cost = budgets.charge("acme-corp", TIER_CHEAP, input_tokens=500, output_tokens=200)
        print(f"acme-corp charged {cost:.5f}, remaining {budgets.remaining_budget('acme-corp'):.5f}")

    try:
        budgets.charge("small-customer", TIER_STRONG, input_tokens=2000, output_tokens=1000)
    except BudgetExceededError as exc:
        print(f"small-customer blocked as expected: {exc}")

    try:
        budgets.charge("unprovisioned-tenant", TIER_CHEAP, input_tokens=100, output_tokens=50)
    except UnknownTenantError as exc:
        print(f"unprovisioned tenant blocked as expected: {exc}")

    print("spend by tenant:", budgets.spend_by_tenant())


if __name__ == "__main__":
    _demo()
