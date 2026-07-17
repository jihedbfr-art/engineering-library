import bisect
import hashlib


class ConsistentHashRing:
    def __init__(self, nodes: list[str], virtual_replicas: int = 100):
        self.ring: dict[int, str] = {}
        self.sorted_keys: list[int] = []
        for node in nodes:
            self.add_node(node, virtual_replicas)

    def _hash(self, key: str) -> int:
        return int(hashlib.md5(key.encode()).hexdigest(), 16)

    def add_node(self, node: str, virtual_replicas: int = 100):
        for i in range(virtual_replicas):
            h = self._hash(f"{node}#{i}")
            self.ring[h] = node
            bisect.insort(self.sorted_keys, h)

    def get_node(self, key: str) -> str:
        h = self._hash(key)
        idx = bisect.bisect(self.sorted_keys, h) % len(self.sorted_keys)
        return self.ring[self.sorted_keys[idx]]


if __name__ == "__main__":
    ring = ConsistentHashRing(["cache-1", "cache-2", "cache-3"])
    for key in ["user:42", "user:99", "session:abc"]:
        print(f"{key} -> {ring.get_node(key)}")
