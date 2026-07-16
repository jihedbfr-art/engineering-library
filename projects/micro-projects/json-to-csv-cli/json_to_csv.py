#!/usr/bin/env python3
"""Convertit une liste d'objets JSON plate en CSV."""
import argparse
import csv
import json
import sys


def load_records(path: str) -> list[dict]:
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    if not isinstance(data, list):
        raise ValueError("le JSON doit être une liste d'objets")
    if not all(isinstance(item, dict) for item in data):
        raise ValueError("chaque élément de la liste doit être un objet JSON")
    return data


def collect_fieldnames(records: list[dict]) -> list[str]:
    fieldnames = []
    for record in records:
        for key in record.keys():
            if key not in fieldnames:
                fieldnames.append(key)
    return fieldnames


def write_csv(records: list[dict], fieldnames: list[str], out) -> None:
    writer = csv.DictWriter(out, fieldnames=fieldnames, extrasaction="ignore")
    writer.writeheader()
    for record in records:
        writer.writerow(record)


def main():
    parser = argparse.ArgumentParser(description="Convertit un JSON (liste d'objets) en CSV.")
    parser.add_argument("input", help="Fichier JSON d'entrée")
    parser.add_argument("-o", "--output", help="Fichier CSV de sortie (défaut: stdout)")
    args = parser.parse_args()

    try:
        records = load_records(args.input)
    except (json.JSONDecodeError, ValueError, FileNotFoundError) as exc:
        parser.error(str(exc))
        return

    if not records:
        parser.error("le JSON est une liste vide, rien à convertir")
        return

    fieldnames = collect_fieldnames(records)

    if args.output:
        with open(args.output, "w", newline="", encoding="utf-8") as out:
            write_csv(records, fieldnames, out)
    else:
        write_csv(records, fieldnames, sys.stdout)


if __name__ == "__main__":
    main()
