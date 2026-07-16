#!/usr/bin/env python3
"""Vérifie si deux chaînes sont des anagrammes, ou cherche des anagrammes dans une liste de mots."""
import argparse
import sys


def normalize(s: str) -> str:
    return "".join(ch.lower() for ch in s if ch.isalnum())


def is_anagram(a: str, b: str) -> bool:
    na, nb = normalize(a), normalize(b)
    return bool(na) and sorted(na) == sorted(nb)


def find_anagram_groups(words: list[str]) -> dict[str, list[str]]:
    groups: dict[str, list[str]] = {}
    for word in words:
        key = "".join(sorted(normalize(word)))
        if not key:
            continue
        groups.setdefault(key, []).append(word)
    return {k: v for k, v in groups.items() if len(v) > 1}


def main() -> None:
    parser = argparse.ArgumentParser(description="Vérifie des anagrammes ou en trouve dans une liste de mots.")
    sub = parser.add_subparsers(dest="command", required=True)

    check = sub.add_parser("check", help="Vérifie si deux mots/phrases sont des anagrammes")
    check.add_argument("first")
    check.add_argument("second")

    find = sub.add_parser("find", help="Trouve des groupes d'anagrammes dans un fichier (un mot par ligne)")
    find.add_argument("wordfile")

    args = parser.parse_args()

    if args.command == "check":
        if is_anagram(args.first, args.second):
            print(f"'{args.first}' et '{args.second}' sont des anagrammes.")
        else:
            print(f"'{args.first}' et '{args.second}' ne sont PAS des anagrammes.")
            sys.exit(1)

    elif args.command == "find":
        try:
            with open(args.wordfile, encoding="utf-8") as f:
                words = [line.strip() for line in f if line.strip()]
        except OSError as e:
            print(f"Erreur: {e}", file=sys.stderr)
            sys.exit(1)

        groups = find_anagram_groups(words)
        if not groups:
            print("Aucun groupe d'anagrammes trouvé.")
            return
        for key, members in groups.items():
            print(", ".join(members))


if __name__ == "__main__":
    main()
