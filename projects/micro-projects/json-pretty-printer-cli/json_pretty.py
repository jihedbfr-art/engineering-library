#!/usr/bin/env python3
"""Réindente proprement un fichier JSON (ou stdin)."""
import argparse
import json
import sys


def main() -> None:
    parser = argparse.ArgumentParser(description="Réindente un JSON depuis un fichier ou stdin.")
    parser.add_argument("file", nargs="?", help="Fichier JSON à lire (défaut: stdin)")
    parser.add_argument("--sort-keys", action="store_true", help="Trier les clés par ordre alphabétique")
    parser.add_argument("--indent", type=int, default=2, help="Nombre d'espaces d'indentation (défaut: 2)")
    parser.add_argument("-o", "--output", help="Fichier de sortie (défaut: stdout)")
    args = parser.parse_args()

    try:
        if args.file:
            with open(args.file, "r", encoding="utf-8") as f:
                data = json.load(f)
        else:
            data = json.load(sys.stdin)
    except json.JSONDecodeError as e:
        print(f"Erreur: JSON invalide - {e}", file=sys.stderr)
        sys.exit(1)
    except FileNotFoundError:
        print(f"Erreur: fichier introuvable - {args.file}", file=sys.stderr)
        sys.exit(1)

    pretty = json.dumps(data, indent=args.indent, sort_keys=args.sort_keys, ensure_ascii=False)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(pretty + "\n")
    else:
        print(pretty)


if __name__ == "__main__":
    main()
