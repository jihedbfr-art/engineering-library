#!/usr/bin/env python3
"""Compare deux fichiers texte et affiche les différences façon `diff` (stdlib difflib)."""
import argparse
import difflib
import sys


def main() -> None:
    parser = argparse.ArgumentParser(description="Compare deux fichiers texte ligne par ligne.")
    parser.add_argument("file1", help="Premier fichier")
    parser.add_argument("file2", help="Second fichier")
    parser.add_argument("-u", "--unified", action="store_true", help="Afficher au format unifié (contexte)")
    args = parser.parse_args()

    try:
        with open(args.file1, encoding="utf-8") as f:
            lines1 = f.readlines()
        with open(args.file2, encoding="utf-8") as f:
            lines2 = f.readlines()
    except OSError as e:
        print(f"Erreur: {e}", file=sys.stderr)
        sys.exit(1)

    if args.unified:
        diff = difflib.unified_diff(lines1, lines2, fromfile=args.file1, tofile=args.file2)
        output = list(diff)
    else:
        diff = difflib.ndiff(lines1, lines2)
        output = [line for line in diff if not line.startswith("? ")]

    if not output:
        print("Les fichiers sont identiques.")
        return

    for line in output:
        sys.stdout.write(line if line.endswith("\n") else line + "\n")

    sys.exit(1)


if __name__ == "__main__":
    main()
