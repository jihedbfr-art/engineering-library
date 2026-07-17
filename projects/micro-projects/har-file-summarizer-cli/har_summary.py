import argparse
import json
from collections import Counter


def summarize(har: dict) -> dict:
    entries = har["log"]["entries"]
    statuses = Counter(e["response"]["status"] for e in entries)
    total_size = sum(e["response"].get("bodySize", 0) for e in entries if e["response"].get("bodySize", 0) > 0)
    slowest = max(entries, key=lambda e: e["time"], default=None)
    return {
        "requests": len(entries),
        "status_codes": dict(statuses),
        "total_body_bytes": total_size,
        "slowest_url": slowest["request"]["url"] if slowest else None,
        "slowest_ms": round(slowest["time"], 1) if slowest else None,
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Resume un fichier HAR (export DevTools reseau)")
    parser.add_argument("har_file")
    args = parser.parse_args()
    with open(args.har_file) as f:
        har = json.load(f)
    for key, value in summarize(har).items():
        print(f"{key}: {value}")
