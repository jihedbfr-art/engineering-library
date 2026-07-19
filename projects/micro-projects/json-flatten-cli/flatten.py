#!/usr/bin/env python3
"""Aplatit un JSON imbrique en cles a plat separees par des points (notation dot).

Utile pour comparer deux JSON ligne a ligne, ou pour deverser une config imbriquee
dans un format .env / variables plates.
"""
import argparse
import json
import sys


def flatten(obj, prefix="", result=None):
    if result is None:
        result = {}

    if isinstance(obj, dict):
        if not obj:
            result[prefix] = {}
        for key, value in obj.items():
            new_prefix = f"{prefix}.{key}" if prefix else str(key)
            flatten(value, new_prefix, result)
    elif isinstance(obj, list):
        if not obj:
            result[prefix] = []
        for index, value in enumerate(obj):
            new_prefix = f"{prefix}[{index}]"
            flatten(value, new_prefix, result)
    else:
        result[prefix] = obj

    return result


def main():
    parser = argparse.ArgumentParser(description="Aplatit un fichier JSON en cles dot-notation.")
    parser.add_argument("file", help="Fichier JSON en entree, ou '-' pour lire depuis stdin")
    parser.add_argument("--format", choices=["json", "env"], default="json",
                         help="Format de sortie : json (defaut) ou env (KEY=value)")
    args = parser.parse_args()

    raw = sys.stdin.read() if args.file == "-" else open(args.file, encoding="utf-8").read()

    try:
        data = json.loads(raw)
    except json.JSONDecodeError as exc:
        print(f"Erreur: JSON invalide - {exc}", file=sys.stderr)
        sys.exit(1)

    flat = flatten(data)

    if args.format == "json":
        print(json.dumps(flat, indent=2, ensure_ascii=False))
    else:
        for key, value in flat.items():
            env_key = key.upper().replace(".", "_").replace("[", "_").replace("]", "")
            print(f"{env_key}={json.dumps(value) if not isinstance(value, (str, int, float, bool)) or value is None else value}")


if __name__ == "__main__":
    main()
