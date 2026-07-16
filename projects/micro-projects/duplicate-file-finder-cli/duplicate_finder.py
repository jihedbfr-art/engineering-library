#!/usr/bin/env python3
"""Scanne un dossier recursivement et trouve les fichiers en doublon (meme contenu)."""
import argparse
import hashlib
import os
import sys
from collections import defaultdict


def hash_file(path, chunk_size=65536):
    """Calcule le sha256 d'un fichier par lecture en chunks (evite de charger le fichier entier en RAM)."""
    h = hashlib.sha256()
    try:
        with open(path, "rb") as f:
            while True:
                chunk = f.read(chunk_size)
                if not chunk:
                    break
                h.update(chunk)
    except (OSError, PermissionError) as e:
        print(f"  [avertissement] impossible de lire {path}: {e}", file=sys.stderr)
        return None
    return h.hexdigest()


def find_duplicates(root_dir):
    """Groupe les fichiers par (taille, hash). On hash uniquement si la taille correspond deja
    a un autre fichier, pour eviter de hasher inutilement tous les fichiers uniques."""
    by_size = defaultdict(list)
    for dirpath, _dirnames, filenames in os.walk(root_dir):
        for name in filenames:
            full_path = os.path.join(dirpath, name)
            try:
                size = os.path.getsize(full_path)
            except OSError:
                continue
            by_size[size].append(full_path)

    duplicates = defaultdict(list)
    for size, paths in by_size.items():
        if len(paths) < 2:
            continue
        by_hash = defaultdict(list)
        for path in paths:
            digest = hash_file(path)
            if digest is None:
                continue
            by_hash[digest].append(path)
        for digest, group in by_hash.items():
            if len(group) > 1:
                duplicates[digest] = group

    return duplicates


def main():
    parser = argparse.ArgumentParser(description="Trouve les fichiers en doublon dans un dossier.")
    parser.add_argument("directory", help="Dossier a scanner recursivement")
    args = parser.parse_args()

    if not os.path.isdir(args.directory):
        print(f"Erreur: '{args.directory}' n'est pas un dossier valide.", file=sys.stderr)
        sys.exit(1)

    duplicates = find_duplicates(args.directory)

    if not duplicates:
        print("Aucun doublon trouve.")
        return

    total_groups = 0
    total_wasted = 0
    for digest, paths in duplicates.items():
        total_groups += 1
        size = os.path.getsize(paths[0])
        wasted = size * (len(paths) - 1)
        total_wasted += wasted
        print(f"\nGroupe de doublons (sha256={digest[:12]}..., {len(paths)} fichiers, {size} octets chacun):")
        for p in paths:
            print(f"  - {p}")

    print(f"\n{total_groups} groupe(s) de doublons trouve(s). Espace gaspille: {total_wasted} octets.")


if __name__ == "__main__":
    main()
