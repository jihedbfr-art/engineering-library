class ReadThroughCache:
    def __init__(self, loader):
        self.loader = loader
        self.cache: dict = {}
        self.hits = self.misses = 0

    def get(self, key):
        if key in self.cache:
            self.hits += 1
            return self.cache[key]
        self.misses += 1
        value = self.loader(key)
        self.cache[key] = value
        return value


if __name__ == "__main__":
    def slow_db_lookup(key):
        print(f"  [DB] lookup couteux pour {key}")
        return f"donnees-de-{key}"

    cache = ReadThroughCache(slow_db_lookup)
    for key in ["user:1", "user:2", "user:1", "user:1", "user:2"]:
        cache.get(key)
    print(f"hits={cache.hits} misses={cache.misses}")
