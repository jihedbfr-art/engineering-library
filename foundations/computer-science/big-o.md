# Big-O — Complexity Analysis

Big-O describes how an algorithm's cost grows as input grows. It's about **scaling**, not stopwatch time.

## The hierarchy (best → worst)

| Notation | Name | n=10 | n=1000 | Example |
|---|---|---|---|---|
| O(1) | constant | 1 | 1 | hash lookup, array index |
| O(log n) | logarithmic | 3 | 10 | binary search |
| O(n) | linear | 10 | 1 000 | scan a list |
| O(n log n) | linearithmic | 33 | 10 000 | good sorts (merge, quick avg) |
| O(n²) | quadratic | 100 | 1 000 000 | nested loops, bubble sort |
| O(2ⁿ) | exponential | 1 024 | 💀 | naive recursion, brute force |
| O(n!) | factorial | 3.6M | 💀💀 | all permutations |

The gap between O(n log n) and O(n²) is the difference between "instant" and "the server melts" at scale.

## How to read your own code

```python
# O(1) — no loop over input
def first(items): return items[0]

# O(n) — one pass
def total(items):
    s = 0
    for x in items: s += x      # n iterations
    return s

# O(n²) — loop inside a loop over the same input
def has_dup(items):
    for i in items:             # n
        for j in items:         # × n
            ...

# O(log n) — halving the search space each step
def bsearch(sorted_items, target):
    lo, hi = 0, len(sorted_items) - 1
    while lo <= hi:
        mid = (lo + hi) // 2    # cuts range in half
        ...
```

Rules of thumb:
- Sequential steps → **add** (drop constants): O(n) + O(n) = O(n).
- Nested over input → **multiply**: loop × loop = O(n²).
- Halving each step → **log**. Halving with work each level → often n log n.
- Keep only the dominant term: O(n² + n) = O(n²).

## Space complexity

Same idea for memory. A recursive function O(n) deep uses O(n) stack. A hash of all items is O(n) space. Often you **trade space for time**: a hash set turns an O(n²) duplicate check into O(n) time + O(n) space.

## The practical takeaways

1. **Constants and small n don't matter** — an O(n²) loop over 10 items is fine. Optimize the hot path with big n.
2. **The classic win**: replace a nested-loop lookup with a hash set/map → O(n²) becomes O(n).
3. **Sorting first** can unlock O(n log n) solutions (dedup, two-pointer, binary search).
4. Measure before micro-optimizing — but *know* the complexity so you don't ship the melting one.
