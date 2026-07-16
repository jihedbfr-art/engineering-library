#!/usr/bin/env python3
"""Renomme en masse les fichiers d'un dossier (préfixe, suffixe ou remplacement regex).

Mode --dry-run par défaut : affiche les renommages prévus sans rien modifier.
Il faut passer --apply explicitement pour effectuer les renommages.
"""
import argparse
import os
import re


def compute_new_name(name: str, args: argparse.Namespace) -> str:
    base, ext = os.path.splitext(name)

    if args.pattern is not None:
        base = re.sub(args.pattern, args.replace or "", base)

    if args.prefix:
        base = args.prefix + base

    if args.suffix:
        base = base + args.suffix

    return base + ext


def collect_files(directory: str, recursive: bool) -> list[str]:
    if recursive:
        files = []
        for root, _, filenames in os.walk(directory):
            for fname in filenames:
                files.append(os.path.relpath(os.path.join(root, fname), directory))
        return files
    return [f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))]


def main():
    parser = argparse.ArgumentParser(
        description="Renomme en masse les fichiers d'un dossier. Dry-run par défaut."
    )
    parser.add_argument("directory", help="Dossier contenant les fichiers à renommer")
    parser.add_argument("--prefix", help="Préfixe à ajouter au nom (sans l'extension)")
    parser.add_argument("--suffix", help="Suffixe à ajouter au nom (sans l'extension)")
    parser.add_argument("--pattern", help="Motif regex à remplacer dans le nom")
    parser.add_argument("--replace", default="", help="Texte de remplacement pour --pattern (défaut: vide)")
    parser.add_argument("--recursive", action="store_true", help="Parcourt aussi les sous-dossiers")
    parser.add_argument(
        "--apply", action="store_true",
        help="Applique réellement les renommages (sans cette option : dry-run, aucune modification)",
    )
    args = parser.parse_args()

    if not args.prefix and not args.suffix and args.pattern is None:
        parser.error("préciser au moins --prefix, --suffix ou --pattern")

    if not os.path.isdir(args.directory):
        parser.error(f"dossier introuvable : {args.directory}")

    files = collect_files(args.directory, args.recursive)
    if not files:
        print("Aucun fichier trouvé.")
        return

    planned = []
    for name in files:
        dirname, basename = os.path.split(name)
        new_basename = compute_new_name(basename, args)
        if new_basename != basename:
            planned.append((name, os.path.join(dirname, new_basename) if dirname else new_basename))

    if not planned:
        print("Aucun renommage nécessaire (les noms générés sont identiques aux originaux).")
        return

    mode = "APPLICATION" if args.apply else "DRY-RUN (aucune modification, utilisez --apply pour renommer)"
    print(f"Mode : {mode}")
    for old, new in planned:
        print(f"  {old}  ->  {new}")

    if not args.apply:
        return

    errors = []
    for old, new in planned:
        old_path = os.path.join(args.directory, old)
        new_path = os.path.join(args.directory, new)
        if os.path.exists(new_path):
            errors.append(f"cible déjà existante, ignoré : {new}")
            continue
        os.makedirs(os.path.dirname(new_path), exist_ok=True) if os.path.dirname(new) else None
        os.rename(old_path, new_path)

    print(f"\n{len(planned) - len(errors)} fichier(s) renommé(s).")
    if errors:
        print(f"{len(errors)} erreur(s) :")
        for err in errors:
            print(f"  - {err}")


if __name__ == "__main__":
    main()
