import time
from datetime import datetime


class Job:
    def __init__(self, name, interval_sec, action):
        self.name = name
        self.interval_sec = interval_sec
        self.action = action
        self.last_run = 0

    def maybe_run(self):
        now = time.time()
        if now - self.last_run >= self.interval_sec:
            self.last_run = now
            self.action()


def cleanup_temp_files():
    print(f"[{datetime.now().isoformat(timespec='seconds')}] nettoyage des fichiers temporaires")


def send_heartbeat():
    print(f"[{datetime.now().isoformat(timespec='seconds')}] heartbeat envoye")


if __name__ == "__main__":
    jobs = [
        Job("cleanup", interval_sec=5, action=cleanup_temp_files),
        Job("heartbeat", interval_sec=2, action=send_heartbeat),
    ]
    print("Daemon demarre (Ctrl+C pour arreter)")
    try:
        while True:
            for job in jobs:
                job.maybe_run()
            time.sleep(0.5)
    except KeyboardInterrupt:
        print("Arret du daemon.")
