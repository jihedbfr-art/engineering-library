import random
import time


def retry_with_backoff(func, max_attempts: int = 5, base_delay: float = 0.5):
    for attempt in range(1, max_attempts + 1):
        try:
            return func()
        except Exception as e:
            if attempt == max_attempts:
                raise
            delay = base_delay * (2 ** (attempt - 1)) + random.uniform(0, 0.1)
            print(f"Tentative {attempt} echouee ({e}), retry dans {delay:.2f}s")
            time.sleep(delay)


if __name__ == "__main__":
    calls = {"count": 0}

    def flaky_call():
        calls["count"] += 1
        if calls["count"] < 3:
            raise ConnectionError("service indisponible")
        return "succes"

    print(retry_with_backoff(flaky_call))
