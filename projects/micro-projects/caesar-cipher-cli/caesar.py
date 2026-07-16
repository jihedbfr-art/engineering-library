#!/usr/bin/env python3
"""Chiffrement/déchiffrement César avec décalage paramétrable et mode brute-force."""
import argparse
import string

ALPHABET_SIZE = 26


def shift_char(c: str, shift: int) -> str:
    if c in string.ascii_lowercase:
        return chr((ord(c) - ord("a") + shift) % ALPHABET_SIZE + ord("a"))
    if c in string.ascii_uppercase:
        return chr((ord(c) - ord("A") + shift) % ALPHABET_SIZE + ord("A"))
    return c


def caesar(text: str, shift: int) -> str:
    return "".join(shift_char(c, shift) for c in text)


def brute_force(text: str) -> None:
    for shift in range(ALPHABET_SIZE):
        print(f"{shift:2d}: {caesar(text, shift)}")


def main():
    parser = argparse.ArgumentParser(description="Chiffre/déchiffre du texte avec le chiffre de César.")
    parser.add_argument("text", help="Texte à traiter")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("-s", "--shift", type=int, help="Décalage (positif pour chiffrer, négatif pour déchiffrer)")
    group.add_argument("-b", "--brute-force", action="store_true", help="Affiche les 26 décalages possibles")
    args = parser.parse_args()

    if args.brute_force:
        brute_force(args.text)
    elif args.shift is not None:
        print(caesar(args.text, args.shift))
    else:
        parser.error("préciser -s/--shift ou -b/--brute-force")


if __name__ == "__main__":
    main()
