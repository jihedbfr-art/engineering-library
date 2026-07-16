#!/usr/bin/env python3
"""Convertit un texte entre camelCase, snake_case, kebab-case et PascalCase,
avec detection automatique du format source."""
import argparse
import re
import sys


def detect_format(text):
    if "_" in text:
        return "snake_case"
    if "-" in text:
        return "kebab-case"
    if re.match(r"^[A-Z]", text):
        return "PascalCase"
    if re.search(r"[A-Z]", text):
        return "camelCase"
    return "lowercase"


def split_words(text, source_format):
    """Decoupe le texte en une liste de mots en minuscules, quel que soit le format source."""
    if source_format in ("snake_case",):
        return [w for w in text.split("_") if w]
    if source_format in ("kebab-case",):
        return [w for w in text.split("-") if w]
    if source_format in ("camelCase", "PascalCase"):
        # insere une frontiere avant chaque majuscule precedee d'une minuscule, ou entre
        # une sequence de majuscules et le mot suivant (ex: "HTTPServer" -> "HTTP Server")
        spaced = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", " ", text)
        spaced = re.sub(r"(?<=[A-Z])(?=[A-Z][a-z])", " ", spaced)
        return [w.lower() for w in spaced.split(" ") if w]
    # lowercase / format inconnu : un seul mot
    return [text.lower()] if text else []


def to_camel_case(words):
    if not words:
        return ""
    return words[0] + "".join(w.capitalize() for w in words[1:])


def to_snake_case(words):
    return "_".join(words)


def to_kebab_case(words):
    return "-".join(words)


def to_pascal_case(words):
    return "".join(w.capitalize() for w in words)


CONVERTERS = {
    "camel": to_camel_case,
    "snake": to_snake_case,
    "kebab": to_kebab_case,
    "pascal": to_pascal_case,
}


def main():
    parser = argparse.ArgumentParser(description="Convertit un texte entre camelCase, snake_case, kebab-case, PascalCase.")
    parser.add_argument("text", help="Texte a convertir")
    parser.add_argument("--to", choices=CONVERTERS.keys(), required=True, help="Format cible")
    args = parser.parse_args()

    if not args.text:
        print("Erreur: texte vide", file=sys.stderr)
        sys.exit(1)

    source_format = detect_format(args.text)
    words = split_words(args.text, source_format)
    result = CONVERTERS[args.to](words)

    print(f"Format detecte: {source_format}")
    print(f"Resultat ({args.to}): {result}")


if __name__ == "__main__":
    main()
