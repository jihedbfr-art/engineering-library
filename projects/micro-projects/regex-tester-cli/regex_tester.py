#!/usr/bin/env python3
"""Teste un pattern regex contre un texte (argument ou fichier), affiche matches, positions et groupes."""
import argparse
import re
import sys


def main() -> None:
    parser = argparse.ArgumentParser(description="Teste une regex contre un texte et affiche les matches détaillés.")
    parser.add_argument("pattern", help="Pattern regex (syntaxe Python re)")

    text_group = parser.add_mutually_exclusive_group(required=True)
    text_group.add_argument("-t", "--text", help="Texte à tester directement")
    text_group.add_argument("-f", "--file", help="Fichier contenant le texte à tester")

    parser.add_argument("-i", "--ignore-case", action="store_true", help="Recherche insensible à la casse")
    parser.add_argument("-m", "--multiline", action="store_true", help="Mode multiligne (^ et $ par ligne)")
    args = parser.parse_args()

    if args.file:
        try:
            with open(args.file, "r", encoding="utf-8") as f:
                text = f.read()
        except FileNotFoundError:
            print(f"Erreur: fichier introuvable - {args.file}", file=sys.stderr)
            sys.exit(1)
    else:
        text = args.text

    flags = 0
    if args.ignore_case:
        flags |= re.IGNORECASE
    if args.multiline:
        flags |= re.MULTILINE

    try:
        pattern = re.compile(args.pattern, flags)
    except re.error as e:
        print(f"Erreur: pattern regex invalide - {e}", file=sys.stderr)
        sys.exit(1)

    matches = list(pattern.finditer(text))

    if not matches:
        print("Aucun match trouvé.")
        sys.exit(1)

    print(f"{len(matches)} match(es) trouvé(s) :\n")
    for i, m in enumerate(matches, start=1):
        print(f"Match {i}: '{m.group(0)}' [{m.start()}:{m.end()}]")
        if m.groups():
            for gi, group in enumerate(m.groups(), start=1):
                print(f"  Groupe {gi}: {group!r}")
        if m.groupdict():
            for name, value in m.groupdict().items():
                print(f"  Groupe nommé '{name}': {value!r}")


if __name__ == "__main__":
    main()
