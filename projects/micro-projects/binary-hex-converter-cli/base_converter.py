#!/usr/bin/env python3
"""Conversion entre binaire, hexadécimal, décimal et octal (stdlib uniquement)."""
import argparse
import sys

BASES = {"bin": 2, "hex": 16, "dec": 10, "oct": 8}
PREFIXES = {"bin": "0b", "hex": "0x", "oct": "0o"}


def parse_value(value: str, base: str) -> int:
    v = value.strip()
    if base == "bin":
        v = v[2:] if v.lower().startswith("0b") else v
    elif base == "hex":
        v = v[2:] if v.lower().startswith("0x") else v
    elif base == "oct":
        v = v[2:] if v.lower().startswith("0o") else v
    return int(v, BASES[base])


def format_value(n: int, base: str) -> str:
    if base == "dec":
        return str(n)
    if base == "bin":
        return format(n, "b")
    if base == "hex":
        return format(n, "x")
    if base == "oct":
        return format(n, "o")
    raise ValueError(f"base inconnue: {base}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Convertit un nombre entre binaire, hexadécimal, décimal et octal.")
    parser.add_argument("value", help="Valeur à convertir (ex: 255, 0xFF, 0b1010, 0o17)")
    parser.add_argument("-f", "--from", dest="from_base", choices=BASES.keys(), default=None,
                         help="Base source (détectée automatiquement si absente via préfixe 0x/0b/0o, sinon décimal)")
    parser.add_argument("-t", "--to", dest="to_base", choices=BASES.keys(), default=None,
                         help="Base cible (si absente, affiche les quatre bases)")
    args = parser.parse_args()

    from_base = args.from_base
    if from_base is None:
        v = args.value.strip().lower()
        if v.startswith("0x"):
            from_base = "hex"
        elif v.startswith("0b"):
            from_base = "bin"
        elif v.startswith("0o"):
            from_base = "oct"
        else:
            from_base = "dec"

    try:
        n = parse_value(args.value, from_base)
    except ValueError:
        print(f"Erreur: '{args.value}' n'est pas une valeur valide en base {from_base}.", file=sys.stderr)
        sys.exit(1)

    if args.to_base:
        print(format_value(n, args.to_base))
    else:
        print(f"decimal: {format_value(n, 'dec')}")
        print(f"binary:  {format_value(n, 'bin')}")
        print(f"hex:     {format_value(n, 'hex')}")
        print(f"octal:   {format_value(n, 'oct')}")


if __name__ == "__main__":
    main()
