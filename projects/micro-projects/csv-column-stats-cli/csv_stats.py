import argparse
import csv
import statistics


def main():
    parser = argparse.ArgumentParser(description="Statistiques (min/max/moyenne/mediane) d'une colonne CSV numerique")
    parser.add_argument("file")
    parser.add_argument("column", help="nom de la colonne")
    args = parser.parse_args()

    with open(args.file, newline="", encoding="utf-8") as f:
        values = [float(row[args.column]) for row in csv.DictReader(f) if row[args.column]]

    print(f"n={len(values)} min={min(values)} max={max(values)} moyenne={statistics.mean(values):.2f} mediane={statistics.median(values)}")


if __name__ == "__main__":
    main()
