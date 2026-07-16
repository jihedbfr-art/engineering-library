#!/usr/bin/env python3
"""Générateur de mot de passe aléatoire sécurisé (stdlib uniquement)."""
import argparse
import secrets
import string
import sys


def build_alphabet(use_symbols: bool, use_digits: bool, use_upper: bool, use_lower: bool) -> str:
    alphabet = ""
    if use_lower:
        alphabet += string.ascii_lowercase
    if use_upper:
        alphabet += string.ascii_uppercase
    if use_digits:
        alphabet += string.digits
    if use_symbols:
        alphabet += "!@#$%^&*()-_=+[]{};:,.?/"
    return alphabet


def generate_password(length: int, alphabet: str) -> str:
    return "".join(secrets.choice(alphabet) for _ in range(length))


def main() -> None:
    parser = argparse.ArgumentParser(description="Génère un mot de passe aléatoire sécurisé.")
    parser.add_argument("-l", "--length", type=int, default=16, help="Longueur du mot de passe (défaut: 16)")
    parser.add_argument("--no-symbols", action="store_true", help="Exclure les symboles")
    parser.add_argument("--no-digits", action="store_true", help="Exclure les chiffres")
    parser.add_argument("--no-upper", action="store_true", help="Exclure les majuscules")
    parser.add_argument("-n", "--count", type=int, default=1, help="Nombre de mots de passe à générer")
    args = parser.parse_args()

    if args.length < 4:
        print("Erreur: la longueur minimale est 4.", file=sys.stderr)
        sys.exit(1)

    alphabet = build_alphabet(
        use_symbols=not args.no_symbols,
        use_digits=not args.no_digits,
        use_upper=not args.no_upper,
        use_lower=True,
    )

    if not alphabet:
        print("Erreur: aucun jeu de caractères sélectionné.", file=sys.stderr)
        sys.exit(1)

    for _ in range(args.count):
        print(generate_password(args.length, alphabet))


if __name__ == "__main__":
    main()
