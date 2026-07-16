import time


def run_stopwatch() -> float:
    input("Appuie sur Entrée pour démarrer...")
    start = time.time()
    input("Appuie sur Entrée pour arrêter...")
    return time.time() - start


if __name__ == "__main__":
    elapsed = run_stopwatch()
    print(f"Temps écoulé : {elapsed:.2f} s")
