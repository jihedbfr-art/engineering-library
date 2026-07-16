#!/usr/bin/env python3
"""Compte mots/lignes/caractères d'un fichier texte, top 10 mots les plus fréquents."""
import argparse
import re
import sys
from collections import Counter


def analyze(text: str):
    lines = text.splitlines()
    chars = len(text)
    words = re.findall(r"[A-Za-zÀ-ÖØ-öø-ÿ0-9']+", text.lower())
    word_count = len(words)
    line_count = len(lines)
    top_words = Counter(words).most_common(10)
    return {
        "lines": line_count,
        "words": word_count,
        "chars": chars,
        "top_words": top_words,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description="Compte mots/lignes/caractères et affiche le top 10 des mots.")
    parser.add_argument("file", nargs="?", help="Fichier texte à analyser (défaut: stdin)")
    args = parser.parse_args()

    try:
        if args.file:
            with open(args.file, "r", encoding="utf-8") as f:
                text = f.read()
        else:
            text = sys.stdin.read()
    except FileNotFoundError:
        print(f"Erreur: fichier introuvable - {args.file}", file=sys.stderr)
        sys.exit(1)

    stats = analyze(text)

    print(f"Lignes     : {stats['lines']}")
    print(f"Mots       : {stats['words']}")
    print(f"Caractères : {stats['chars']}")
    print("\nTop 10 des mots les plus fréquents :")
    if not stats["top_words"]:
        print("  (aucun mot)")
    else:
        for word, count in stats["top_words"]:
            print(f"  {word:<20} {count}")


if __name__ == "__main__":
    main()
