import time
from collections import deque


class SlidingWindowRateLimiter:
    def __init__(self, max_requests: int, window_seconds: float):
        self.max_requests = max_requests
        self.window_seconds = window_seconds
        self.requests: dict[str, deque] = {}

    def allow(self, client_id: str) -> bool:
        now = time.time()
        q = self.requests.setdefault(client_id, deque())
        while q and q[0] <= now - self.window_seconds:
            q.popleft()
        if len(q) >= self.max_requests:
            return False
        q.append(now)
        return True


if __name__ == "__main__":
    limiter = SlidingWindowRateLimiter(max_requests=3, window_seconds=5)
    for i in range(5):
        print(f"Requete {i + 1}: {'autorisee' if limiter.allow('client-A') else 'rejetee (429)'}")
