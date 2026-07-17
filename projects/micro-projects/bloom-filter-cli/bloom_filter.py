import hashlib


class BloomFilter:
    def __init__(self, size: int = 1000, hash_count: int = 3):
        self.size = size
        self.hash_count = hash_count
        self.bits = [False] * size

    def _hashes(self, item: str) -> list[int]:
        return [
            int(hashlib.md5(f"{item}{i}".encode()).hexdigest(), 16) % self.size
            for i in range(self.hash_count)
        ]

    def add(self, item: str):
        for h in self._hashes(item):
            self.bits[h] = True

    def might_contain(self, item: str) -> bool:
        return all(self.bits[h] for h in self._hashes(item))


if __name__ == "__main__":
    bf = BloomFilter()
    bf.add("alice@example.com")
    bf.add("bob@example.com")

    for email in ["alice@example.com", "carol@example.com"]:
        print(f"{email}: {'possiblement present' if bf.might_contain(email) else 'certainement absent'}")
