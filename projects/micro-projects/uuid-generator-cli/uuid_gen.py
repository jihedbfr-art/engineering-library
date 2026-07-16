#!/usr/bin/env python3
"""Génère des UUID v4 en ligne de commande."""
import argparse
import uuid


def generate(count: int, uppercase: bool = False) -> list[str]:
    ids = [str(uuid.uuid4()) for _ in range(count)]
    if uppercase:
        ids = [i.upper() for i in ids]
    return ids


def main():
    parser = argparse.ArgumentParser(description="Génère un ou plusieurs UUID v4.")
    parser.add_argument("-n", "--number", type=int, default=1, help="Nombre d'UUID à générer (défaut: 1)")
    parser.add_argument("-u", "--uppercase", action="store_true", help="Affiche les UUID en majuscules")
    args = parser.parse_args()

    if args.number < 1:
        parser.error("le nombre d'UUID doit être >= 1")

    for value in generate(args.number, args.uppercase):
        print(value)


if __name__ == "__main__":
    main()
