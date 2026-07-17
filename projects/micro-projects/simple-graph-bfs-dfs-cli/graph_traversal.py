from collections import deque


def bfs(graph: dict, start: str) -> list[str]:
    visited, order, queue = {start}, [], deque([start])
    while queue:
        node = queue.popleft()
        order.append(node)
        for neighbor in graph.get(node, []):
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
    return order


def dfs(graph: dict, start: str, visited: set = None) -> list[str]:
    visited = visited if visited is not None else set()
    visited.add(start)
    order = [start]
    for neighbor in graph.get(start, []):
        if neighbor not in visited:
            order.extend(dfs(graph, neighbor, visited))
    return order


if __name__ == "__main__":
    graph = {"A": ["B", "C"], "B": ["D"], "C": ["D"], "D": []}
    print("BFS:", bfs(graph, "A"))
    print("DFS:", dfs(graph, "A"))
