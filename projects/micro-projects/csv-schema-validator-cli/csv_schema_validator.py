import argparse
import csv
import json


def validate(rows: list[dict], schema: dict) -> list[str]:
    errors = []
    for i, row in enumerate(rows, start=2):
        for col, rule in schema.items():
            value = row.get(col, "")
            if rule.get("required") and not value.strip():
                errors.append(f"Ligne {i}: '{col}' manquant")
            elif value and rule.get("type") == "int" and not value.isdigit():
                errors.append(f"Ligne {i}: '{col}' devrait etre un entier (recu: '{value}')")
    return errors


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Valide un CSV contre un schema JSON simple")
    parser.add_argument("csv_file")
    parser.add_argument("schema_file", help='JSON: {"colonne": {"required": true, "type": "int"}}')
    args = parser.parse_args()
    with open(args.csv_file, newline="") as f:
        rows = list(csv.DictReader(f))
    with open(args.schema_file) as f:
        schema = json.load(f)
    errors = validate(rows, schema)
    print(f"{len(errors)} erreur(s)" if errors else "CSV valide.")
    for e in errors:
        print(f"  - {e}")
