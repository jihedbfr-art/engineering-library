# Data Structures — What to Use When

Picking the right structure is half of writing fast code. Here's the decision map.

## The cheat table

| Structure | Access | Search | Insert | Delete | Use when |
|---|---|---|---|---|---|
| **Array / List** | O(1) | O(n) | O(n) | O(n) | ordered data, index access, iteration |
| **Hash map/set** | — | O(1)* | O(1)* | O(1)* | lookups by key, dedup, counting |
| **Linked list** | O(n) | O(n) | O(1)† | O(1)† | frequent insert/delete at ends |
| **Stack (LIFO)** | O(1) top | — | O(1) | O(1) | undo, DFS, call stack, backtracking |
| **Queue (FIFO)** | O(1) ends | — | O(1) | O(1) | BFS, task scheduling, buffers |
| **Binary search tree** | — | O(log n)‡ | O(log n)‡ | O(log n)‡ | sorted data + fast search |
| **Heap** | O(1) min/max | — | O(log n) | O(log n) | priority queue, top-k, scheduling |
| **Graph** | — | varies | — | — | networks, dependencies, maps |

\* average; O(n) worst case with collisions. † if you hold the node. ‡ balanced tree; degrades to O(n) if unbalanced.

## Hash maps — your most-used power tool

```python
# Counting (frequency)
counts = {}
for word in words:
    counts[word] = counts.get(word, 0) + 1

# Dedup in one pass, O(n)
seen = set()
for x in items:
    if x in seen: ...        # O(1) membership
    seen.add(x)

# Two-sum in O(n) instead of O(n²)
need = {}
for i, x in enumerate(nums):
    if target - x in need: return [need[target-x], i]
    need[x] = i
```
If you're writing a nested loop to "find matching X", a hash map probably removes the inner loop.

## Stack & queue — direction matters

- **Stack** (push/pop same end): matching brackets, DFS, undo/redo, expression eval, backtracking.
- **Queue** (add one end, remove other): BFS, producer/consumer, rate limiting, print/task queues.
- **Deque**: both ends — sliding-window problems, work-stealing.

## Trees & heaps

- **Binary search tree / balanced tree** (red-black, AVL): keeps data sorted with O(log n) ops. Most languages' "ordered map/set" are trees.
- **Heap / priority queue**: always gives you the min (or max) fast. Use for: Dijkstra, top-k largest, merge k sorted lists, task scheduling by priority.
- **Trie**: prefix tree for autocomplete, dictionaries, IP routing.

## Graphs

Represent as **adjacency list** (`{node: [neighbors]}`) for sparse graphs — memory-efficient and the common default. Adjacency matrix only for dense graphs or O(1) edge checks. Traversal: BFS (shortest path in unweighted), DFS (cycles, topological sort, connectivity).

## The mental checklist

1. Need lookup by key? → **hash map**.
2. Need order + fast search? → **tree / sorted structure**.
3. Need "next most important"? → **heap**.
4. Process in order of arrival / by level? → **queue**.
5. Process most-recent-first / backtrack? → **stack**.
6. Relationships between things? → **graph**.
