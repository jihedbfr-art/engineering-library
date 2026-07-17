import argparse
import re


def analyze(schema: str) -> dict:
    types = re.findall(r"type (\w+)", schema)
    queries = re.findall(r"^\s*(\w+)\(.*\):", schema, re.MULTILINE)
    fields = re.findall(r"^\s+\w+:\s*[\w!\[\]]+", schema, re.MULTILINE)
    return {"types": len(types), "type_names": types, "fields": len(fields)}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Compte les types/champs d'un schema GraphQL")
    parser.add_argument("schema_file")
    args = parser.parse_args()
    with open(args.schema_file) as f:
        stats = analyze(f.read())
    print(f"Types: {stats['types']} ({', '.join(stats['type_names'])})")
    print(f"Champs (approx): {stats['fields']}")
