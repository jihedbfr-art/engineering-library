import argparse
import json


def validate(data, schema, path="root") -> list[str]:
    errors = []
    expected_type = schema.get("type")
    type_map = {"string": str, "number": (int, float), "integer": int, "boolean": bool, "object": dict, "array": list}

    if expected_type and expected_type in type_map:
        if not isinstance(data, type_map[expected_type]):
            errors.append(f"{path}: attendu {expected_type}, recu {type(data).__name__}")
            return errors

    if expected_type == "object":
        for key in schema.get("required", []):
            if key not in data:
                errors.append(f"{path}: propriete requise manquante '{key}'")
        for key, subschema in schema.get("properties", {}).items():
            if key in data:
                errors.extend(validate(data[key], subschema, f"{path}.{key}"))

    if expected_type == "array":
        item_schema = schema.get("items")
        if item_schema:
            for i, item in enumerate(data):
                errors.extend(validate(item, item_schema, f"{path}[{i}]"))

    return errors


def main():
    parser = argparse.ArgumentParser(description="Valide un JSON contre un schema minimal (type/required/properties)")
    parser.add_argument("data_file")
    parser.add_argument("schema_file")
    args = parser.parse_args()

    with open(args.data_file) as f:
        data = json.load(f)
    with open(args.schema_file) as f:
        schema = json.load(f)

    errors = validate(data, schema)
    if errors:
        print(f"{len(errors)} erreur(s) :")
        for e in errors:
            print(f"  - {e}")
    else:
        print("Valide.")


if __name__ == "__main__":
    main()
