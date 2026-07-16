#!/usr/bin/env python3
"""Conversion entier <-> chiffres romains (bornes 1-3999), stdlib uniquement."""
import argparse
import sys

VALUES = [
    (1000, "M"), (900, "CM"), (500, "D"), (400, "CD"),
    (100, "C"), (90, "XC"), (50, "L"), (40, "XL"),
    (10, "X"), (9, "IX"), (5, "V"), (4, "IV"), (1, "I"),
]

ROMAN_TO_INT = {"I": 1, "V": 5, "X": 10, "L": 50, "C": 100, "D": 500, "M": 1000}


def int_to_roman(n: int) -> str:
    if not (1 <= n <= 3999):
        raise ValueError("le nombre doit être compris entre 1 et 3999")
    result = []
    remaining = n
    for value, symbol in VALUES:
        while remaining >= value:
            result.append(symbol)
            remaining -= value
    return "".join(result)


def roman_to_int(s: str) -> int:
    s = s.strip().upper()
    if not s:
        raise ValueError("chaîne vide")
    for ch in s:
        if ch not in ROMAN_TO_INT:
            raise ValueError(f"caractère romain invalide: '{ch}'")

    total = 0
    prev_value = 0
    for ch in reversed(s):
        value = ROMAN_TO_INT[ch]
        if value < prev_value:
            total -= value
        else:
            total += value
            prev_value = value

    if not (1 <= total <= 3999) or int_to_roman(total) != s:
        raise ValueError(f"'{s}' n'est pas un chiffre romain valide")

    return total


def main() -> None:
    parser = argparse.ArgumentParser(description="Convertit entre entiers et chiffres romains (1-3999).")
    parser.add_argument("value", help="Entier (ex: 1994) ou chiffre romain (ex: MCMXCIV)")
    args = parser.parse_args()

    raw = args.value.strip()
    try:
        if raw.lstrip("-").isdigit():
            n = int(raw)
            print(int_to_roman(n))
        else:
            print(roman_to_int(raw))
    except ValueError as e:
        print(f"Erreur: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
