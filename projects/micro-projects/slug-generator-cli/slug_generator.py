#!/usr/bin/env python3
"""Convertit un texte en slug URL-friendly : minuscules, tirets, sans accents ni caracteres speciaux."""
import argparse
import re
import unicodedata


def slugify(text):
    # normalise en decomposant les caracteres accentues (NFKD) puis supprime les diacritiques
    normalized = unicodedata.normalize("NFKD", text)
    without_accents = "".join(c for c in normalized if not unicodedata.combining(c))

    lowered = without_accents.lower()

    # remplace tout ce qui n'est pas alphanumerique par un tiret
    slug = re.sub(r"[^a-z0-9]+", "-", lowered)

    # supprime les tirets en debut/fin et fusionne les tirets multiples
    slug = re.sub(r"-+", "-", slug).strip("-")

    return slug


def main():
    parser = argparse.ArgumentParser(description="Convertit un texte en slug URL-friendly.")
    parser.add_argument("text", help="Texte a convertir en slug")
    args = parser.parse_args()

    print(slugify(args.text))


if __name__ == "__main__":
    main()
