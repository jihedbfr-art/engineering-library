import argparse
import csv


def main():
    parser = argparse.ArgumentParser(description="Fusionne plusieurs CSV (memes colonnes) en un seul")
    parser.add_argument("files", nargs="+")
    parser.add_argument("-o", "--output", default="merged.csv")
    args = parser.parse_args()

    rows = []
    fieldnames = None
    for path in args.files:
        with open(path, newline="", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            fieldnames = fieldnames or reader.fieldnames
            rows.extend(reader)

    with open(args.output, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    print(f"{len(rows)} lignes fusionnees dans {args.output}")


if __name__ == "__main__":
    main()
