# Algorithms — The Essential Toolkit

You rarely implement these from scratch at work — but knowing them shapes how you solve everything.

## Searching

```
Linear search    O(n)      unsorted data, just scan
Binary search    O(log n)  SORTED data — halve the range each step
```
Binary search's idea (halving a search space) reappears everywhere: guessing games, finding a bug via git bisect, tuning a threshold.

## Sorting (know the trade-offs, not the code)

| Algorithm | Time | Space | Stable | Note |
|---|---|---|---|---|
| Quicksort | O(n log n) avg, O(n²) worst | O(log n) | no | fast in practice, in-place |
| Mergesort | O(n log n) always | O(n) | yes | predictable, great for linked lists |
| Heapsort | O(n log n) | O(1) | no | in-place, no worst-case blowup |
| Insertion | O(n²) | O(1) | yes | tiny/nearly-sorted arrays |

**In real life you call the built-in sort** (usually Timsort — a merge/insertion hybrid, O(n log n), stable). Know *when* it matters: sorting first often unlocks cheaper algorithms.

## Graph traversal

```
BFS (queue)  — level by level → shortest path in UNWEIGHTED graphs
DFS (stack/recursion) — go deep → cycle detection, topological sort, connectivity
Dijkstra (heap) — shortest path with NON-NEGATIVE weights
```

```python
from collections import deque
def bfs(graph, start):
    seen, q = {start}, deque([start])
    while q:
        node = q.popleft()
        for nb in graph[node]:
            if nb not in seen:
                seen.add(nb); q.append(nb)
```

## Two pointers & sliding window

Turn many O(n²) problems into O(n):
- **Two pointers**: sorted array, move from both ends (pair sums, palindromes, container problems).
- **Sliding window**: contiguous subarray/substring problems (longest substring without repeats, max sum of k elements).

## Recursion & dynamic programming

- **Recursion**: a function calling itself on a smaller problem + a base case. Trees, backtracking, divide-and-conquer.
- **Dynamic programming**: recursion + remembering results (memoization) so you don't recompute. Turns exponential into polynomial.

```python
# Fibonacci: O(2^n) naive → O(n) memoized
from functools import lru_cache
@lru_cache(None)
def fib(n): return n if n < 2 else fib(n-1) + fib(n-2)
```
DP smell test: overlapping subproblems + optimal substructure (knapsack, edit distance, coin change, longest common subsequence).

## Greedy

Make the locally best choice at each step. Works when local optimum → global optimum (interval scheduling, Huffman coding, Dijkstra). When it doesn't, you need DP.

## How to approach any algorithm problem

1. Restate the problem; write examples including edge cases (empty, one element, duplicates).
2. Brute force first — get *a* correct answer, note its complexity.
3. Spot the bottleneck → which structure/technique removes it? (hash? sort? two pointers? DP?)
4. Code it, test the edge cases, then state the final time/space complexity.
