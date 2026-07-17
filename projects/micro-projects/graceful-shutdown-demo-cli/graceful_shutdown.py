import signal
import sys
import time

shutting_down = False


def handle_sigterm(signum, frame):
    global shutting_down
    print("\nSIGTERM recu - arret propre en cours (fin des requetes en vol, fermeture des connexions)...")
    shutting_down = True


def main():
    signal.signal(signal.SIGTERM, handle_sigterm)
    signal.signal(signal.SIGINT, handle_sigterm)

    print("Service demarre (PID visible via ps). Ctrl+C pour simuler un arret K8s propre.")
    tick = 0
    while not shutting_down:
        tick += 1
        print(f"traitement de requetes... tick={tick}")
        time.sleep(1)
        if tick >= 3 and not shutting_down:
            continue

    print("Nettoyage termine, sortie propre.")
    sys.exit(0)


if __name__ == "__main__":
    main()
