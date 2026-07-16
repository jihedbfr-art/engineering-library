#!/usr/bin/env python3
"""Affiche l'arborescence d'un dossier façon `tree`, avec profondeur max optionnelle."""
import argparse
import os
import sys

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

BRANCH = "├── "
LAST_BRANCH = "└── "
PIPE = "│   "
SPACE = "    "


def walk(directory: str, prefix: str, depth: int, max_depth: int | None, show_hidden: bool) -> tuple[int, int]:
    dirs_count = 0
    files_count = 0

    if max_depth is not None and depth >= max_depth:
        return dirs_count, files_count

    try:
        entries = sorted(os.listdir(directory), key=str.lower)
    except PermissionError:
        return dirs_count, files_count

    if not show_hidden:
        entries = [e for e in entries if not e.startswith(".")]

    for i, entry in enumerate(entries):
        path = os.path.join(directory, entry)
        is_last = i == len(entries) - 1
        connector = LAST_BRANCH if is_last else BRANCH
        print(prefix + connector + entry)

        if os.path.isdir(path):
            dirs_count += 1
            extension = SPACE if is_last else PIPE
            sub_dirs, sub_files = walk(path, prefix + extension, depth + 1, max_depth, show_hidden)
            dirs_count += sub_dirs
            files_count += sub_files
        else:
            files_count += 1

    return dirs_count, files_count


def main():
    parser = argparse.ArgumentParser(description="Affiche l'arborescence d'un dossier.")
    parser.add_argument("directory", nargs="?", default=".", help="Dossier à afficher (défaut: .)")
    parser.add_argument("-L", "--max-depth", type=int, help="Profondeur maximale à afficher")
    parser.add_argument("-a", "--all", action="store_true", help="Inclut les fichiers/dossiers cachés (commençant par .)")
    args = parser.parse_args()

    if not os.path.isdir(args.directory):
        parser.error(f"dossier introuvable : {args.directory}")

    print(args.directory)
    dirs_count, files_count = walk(args.directory, "", 0, args.max_depth, args.all)
    print(f"\n{dirs_count} dossier(s), {files_count} fichier(s)")


if __name__ == "__main__":
    main()
