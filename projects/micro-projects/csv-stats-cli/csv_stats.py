import argparse
import csv
import statistics


def compute_stats(rows: list[dict], column: str) -> dict:
    values = [float(r[column]) for r in rows if r.get(column, "").strip()]
    if not values:
        return {}
    return {
        "count": len(values),
        "min": min(values),
        "max": max(values),
        "mean": round(statistics.mean(values), 2),
        "median": statistics.median(values),
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Statistiques d'une colonne numerique dans un CSV")
    parser.add_argument("file")
    parser.add_argument("column")
    args = parser.parse_args()
    with open(args.file, newline="") as f:
        rows = list(csv.DictReader(f))
    stats = compute_stats(rows, args.column)
    for key, value in stats.items():
        print(f"{key}: {value}")
