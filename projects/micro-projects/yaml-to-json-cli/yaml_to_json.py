import argparse
import json
import sys


def parse_simple_yaml(text: str) -> dict:
    """Parseur YAML minimal : cle: valeur, sans imbrication ni listes complexes."""
    result = {}
    for line in text.splitlines():
        line = line.strip()
        if not line or line.startswith("#") or ":" not in line:
            continue
        key, _, value = line.partition(":")
        value = value.strip()
        if value.lower() in ("true", "false"):
            value = value.lower() == "true"
        elif value.replace(".", "", 1).isdigit():
            value = float(value) if "." in value else int(value)
        else:
            value = value.strip("'\"")
        result[key.strip()] = value
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Convertit un YAML plat (cle: valeur) en JSON")
    parser.add_argument("file", help="fichier .yaml/.yml")
    args = parser.parse_args()
    with open(args.file) as f:
        data = parse_simple_yaml(f.read())
    json.dump(data, sys.stdout, indent=2, ensure_ascii=False)
    print()
