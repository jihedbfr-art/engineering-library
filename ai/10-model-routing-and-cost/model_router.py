"""Rule-based model routing, a semantic cache built on 09-vector-layer's VectorStore,
and a cost tracker. See model-routing-and-cost-control.md for why routing starts as
rules (same reasoning as the guard chain in 08-guards-safety) and why the cache
threshold is deliberately conservative.

Install: pip install -r ../requirements.txt
Run:     python model_router.py
"""

from __future__ import annotations

import sys
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from shared.utils import get_logger, timed  # noqa: E402

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "09-vector-layer"))
from vector_store import VectorRecord, VectorStore  # noqa: E402

logger = get_logger(__name__)


@dataclass
class ModelTier:
    name: str
    cost_per_1k_input_tokens: float
    cost_per_1k_output_tokens: float


# Illustrative tiers, not live pricing — plug in real numbers per provider/model.
TIER_CHEAP = ModelTier("cheap-fast", cost_per_1k_input_tokens=0.0002, cost_per_1k_output_tokens=0.0008)
TIER_STRONG = ModelTier("strong-reasoning", cost_per_1k_input_tokens=0.003, cost_per_1k_output_tokens=0.015)

_HIGH_STAKES_KEYWORDS = ("refund", "cancel my account", "legal", "chargeback", "unauthorized charge")


@dataclass
class RoutingDecision:
    tier: ModelTier
    reason: str


class ModelRouter:
    """Rule-based routing: short-circuits to the strong tier on high-stakes
    keywords or long/complex-looking requests, otherwise routes to the cheap
    tier. Replace individual rules with a trained classifier once you have
    enough labeled misroutes to train one — not before.
    """

    def __init__(self, *, long_request_chars: int = 600):
        self.long_request_chars = long_request_chars

    def route(self, request_text: str) -> RoutingDecision:
        lowered = request_text.lower()
        for keyword in _HIGH_STAKES_KEYWORDS:
            if keyword in lowered:
                return RoutingDecision(tier=TIER_STRONG, reason=f"high-stakes keyword: '{keyword}'")
        if len(request_text) > self.long_request_chars:
            return RoutingDecision(tier=TIER_STRONG, reason=f"request over {self.long_request_chars} chars, likely multi-step")
        return RoutingDecision(tier=TIER_CHEAP, reason="no high-stakes signal, routing to cheap tier")


@dataclass
class CacheHit:
    cached_answer: str
    similarity: float


class SemanticCache:
    """A semantic cache on top of VectorStore: embed the request, search prior
    answered requests by similarity, return a cached answer only above a
    conservative threshold. See the module doc for why loose thresholds are
    the actual failure mode here, not cache misses.
    """

    def __init__(self, embed_fn, *, similarity_threshold: float = 0.8):
        self._embed = embed_fn
        self._threshold = similarity_threshold
        self._store = VectorStore.create(backend="memory")
        self._answers: dict[str, str] = {}

    def get(self, request_text: str) -> CacheHit | None:
        query_vector = self._embed(request_text)
        matches = self._store.search(query_vector, top_k=1)
        if not matches or matches[0].score < self._threshold:
            return None
        record_id = matches[0].record.id
        return CacheHit(cached_answer=self._answers[record_id], similarity=matches[0].score)

    def put(self, request_text: str, answer: str) -> None:
        record_id = f"cache-{len(self._answers)}"
        self._answers[record_id] = answer
        self._store.upsert([VectorRecord(id=record_id, vector=self._embed(request_text), text=request_text)])


@dataclass
class CostTracker:
    calls: list[dict] = field(default_factory=list)

    def record(self, tier: ModelTier, input_tokens: int, output_tokens: int) -> float:
        cost = (
            input_tokens / 1000 * tier.cost_per_1k_input_tokens
            + output_tokens / 1000 * tier.cost_per_1k_output_tokens
        )
        self.calls.append({"tier": tier.name, "input_tokens": input_tokens, "output_tokens": output_tokens, "cost": cost})
        return cost

    def total_cost(self) -> float:
        return sum(c["cost"] for c in self.calls)

    def cost_by_tier(self) -> dict[str, float]:
        totals: dict[str, float] = {}
        for call in self.calls:
            totals[call["tier"]] = totals.get(call["tier"], 0.0) + call["cost"]
        return totals


def _fake_embed(text: str, dims: int = 32):
    """A hashed bag-of-words vector, not a real embedding — good enough to make
    word-overlapping requests land close in cosine similarity for this demo.
    Real code embeds with an actual model; see
    ../01-foundations/tokenization-and-embeddings.md.
    """
    import numpy as np
    vector = np.zeros(dims)
    for word in text.lower().split():
        vector[hash(word) % dims] += 1.0
    return vector.tolist()


def _fake_answer(request_text: str, tier: ModelTier) -> str:
    return f"[{tier.name}] answer to: {request_text[:40]}..."


def _demo() -> None:
    router = ModelRouter()
    cache = SemanticCache(embed_fn=_fake_embed)
    costs = CostTracker()

    requests = [
        "What are your store hours?",
        "What are your store hours on weekends?",  # near-duplicate of the first, should hit cache
        "I want to dispute an unauthorized charge on my account from last week.",
    ]

    for request_text in requests:
        cached = cache.get(request_text)
        if cached is not None:
            print(f"[CACHE HIT sim={cached.similarity:.3f}] {request_text!r} -> {cached.cached_answer}")
            continue

        decision = router.route(request_text)
        with timed(f"call [{decision.tier.name}]", logger):
            answer = _fake_answer(request_text, decision.tier)
        cost = costs.record(decision.tier, input_tokens=len(request_text), output_tokens=len(answer))
        cache.put(request_text, answer)
        print(f"[{decision.tier.name}, {decision.reason}] {request_text!r} -> {answer} (${cost:.6f})")

    print(f"\ntotal cost: ${costs.total_cost():.6f}")
    print(f"cost by tier: {costs.cost_by_tier()}")


if __name__ == "__main__":
    _demo()
