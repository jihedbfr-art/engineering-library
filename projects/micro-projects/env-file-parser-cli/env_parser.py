#!/usr/bin/env python3
"""Parse un fichier .env, valide la syntaxe et détecte les doublons de clés."""
import argparse
import json
import re

KEY_VALUE_RE = re.compile(r"^([A-Za-z_][A-Za-z0-9_]*)=(.*)$")


def strip_quotes(value: str) -> str:
    if len(value) >= 2 and value[0] == value[-1] and value[0] in ("'", '"'):
        return value[1:-1]
    return value


def parse_env(lines: list[str]) -> tuple[dict, list[str], list[str]]:
    values: dict = {}
    duplicates: list[str] = []
    errors: list[str] = []

    for lineno, raw_line in enumerate(lines, start=1):
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue

        match = KEY_VALUE_RE.match(line)
        if not match:
            errors.append(f"ligne {lineno}: syntaxe invalide -> {raw_line.rstrip()!r}")
            continue

        key, value = match.group(1), strip_quotes(match.group(2).strip())

        if key in values:
            duplicates.append(f"{key} (redéfini ligne {lineno})")

        values[key] = value

    return values, duplicates, errors


def main():
    parser = argparse.ArgumentParser(description="Parse et valide un fichier .env.")
    parser.add_argument("file", help="Fichier .env à parser")
    parser.add_argument("--export-json", help="Exporte les clés/valeurs vers un fichier JSON")
    args = parser.parse_args()

    with open(args.file, "r", encoding="utf-8") as f:
        lines = f.readlines()

    values, duplicates, errors = parse_env(lines)

    print(f"{len(values)} clé(s) valide(s) trouvée(s).")
    for key, value in values.items():
        print(f"  {key}={value}")

    if duplicates:
        print(f"\n{len(duplicates)} doublon(s) de clé détecté(s) :")
        for dup in duplicates:
            print(f"  - {dup}")

    if errors:
        print(f"\n{len(errors)} ligne(s) invalide(s) :")
        for err in errors:
            print(f"  - {err}")

    if args.export_json:
        with open(args.export_json, "w", encoding="utf-8") as out:
            json.dump(values, out, indent=2, ensure_ascii=False)
        print(f"\nExporté vers {args.export_json}")


if __name__ == "__main__":
    main()
