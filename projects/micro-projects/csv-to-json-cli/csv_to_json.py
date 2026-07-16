#!/usr/bin/env python3
"""Convertit un fichier CSV en JSON (liste d'objets), en-têtes automatiques."""
import argparse
import csv
import json
import sys


def convert(input_path: str, delimiter: str) -> list:
    with open(input_path, "r", encoding="utf-8", newline="") as f:
        reader = csv.DictReader(f, delimiter=delimiter)
        return [dict(row) for row in reader]


def main() -> None:
    parser = argparse.ArgumentParser(description="Convertit un CSV en JSON (liste d'objets).")
    parser.add_argument("file", nargs="?", help="Fichier CSV à lire (défaut: stdin)")
    parser.add_argument("-o", "--output", help="Fichier JSON de sortie (défaut: stdout)")
    parser.add_argument("-d", "--delimiter", default=",", help="Délimiteur CSV (défaut: ',')")
    parser.add_argument("--indent", type=int, default=2, help="Indentation JSON (défaut: 2)")
    args = parser.parse_args()

    try:
        if args.file:
            rows = convert(args.file, args.delimiter)
        else:
            reader = csv.DictReader(sys.stdin, delimiter=args.delimiter)
            rows = [dict(row) for row in reader]
    except FileNotFoundError:
        print(f"Erreur: fichier introuvable - {args.file}", file=sys.stderr)
        sys.exit(1)

    output_json = json.dumps(rows, indent=args.indent, ensure_ascii=False)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(output_json + "\n")
    else:
        print(output_json)


if __name__ == "__main__":
    main()
