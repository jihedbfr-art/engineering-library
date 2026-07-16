#!/usr/bin/env python3
"""Parse un fichier de log au format Apache/Nginx access log courant (Combined Log Format)
et affiche des statistiques : requetes par code HTTP, top IPs, top endpoints."""
import argparse
import re
import sys
from collections import Counter

# Format Combined Log courant :
# 127.0.0.1 - - [10/Oct/2023:13:55:36 +0000] "GET /index.html HTTP/1.1" 200 2326 "-" "Mozilla/5.0"
LOG_PATTERN = re.compile(
    r'(?P<ip>\S+)\s+\S+\s+\S+\s+\[(?P<datetime>[^\]]+)\]\s+'
    r'"(?P<method>\S+)\s+(?P<endpoint>\S+)\s+\S+"\s+'
    r'(?P<status>\d{3})\s+(?P<size>\S+)'
)


def parse_log_file(path):
    entries = []
    skipped = 0
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            match = LOG_PATTERN.search(line)
            if not match:
                skipped += 1
                continue
            entries.append(match.groupdict())
    return entries, skipped


def analyze(entries, top_n=10):
    status_counter = Counter(e["status"] for e in entries)
    ip_counter = Counter(e["ip"] for e in entries)
    endpoint_counter = Counter(e["endpoint"] for e in entries)

    return {
        "total": len(entries),
        "by_status": status_counter.most_common(),
        "top_ips": ip_counter.most_common(top_n),
        "top_endpoints": endpoint_counter.most_common(top_n),
    }


def print_report(stats, top_n):
    print(f"Total de requetes analysees : {stats['total']}\n")

    print("=== Requetes par code HTTP ===")
    for status, count in sorted(stats["by_status"], key=lambda x: -x[1]):
        print(f"  {status}: {count}")

    print(f"\n=== Top {top_n} IPs ===")
    for ip, count in stats["top_ips"]:
        print(f"  {ip}: {count} requete(s)")

    print(f"\n=== Top {top_n} endpoints ===")
    for endpoint, count in stats["top_endpoints"]:
        print(f"  {endpoint}: {count} requete(s)")


def main():
    parser = argparse.ArgumentParser(description="Analyse un fichier de log Apache/Nginx (Combined Log Format).")
    parser.add_argument("logfile", help="Chemin du fichier de log")
    parser.add_argument("--top", type=int, default=10, help="Nombre d'entrees dans les tops (defaut: 10)")
    args = parser.parse_args()

    try:
        entries, skipped = parse_log_file(args.logfile)
    except OSError as e:
        print(f"Erreur: {e}", file=sys.stderr)
        sys.exit(1)

    if not entries:
        print("Aucune ligne de log valide trouvee.")
        if skipped:
            print(f"({skipped} ligne(s) ignoree(s), format non reconnu)")
        return

    stats = analyze(entries, args.top)
    print_report(stats, args.top)

    if skipped:
        print(f"\n({skipped} ligne(s) ignoree(s), format non reconnu)")


if __name__ == "__main__":
    main()
