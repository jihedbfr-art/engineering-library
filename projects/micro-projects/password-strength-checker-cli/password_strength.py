#!/usr/bin/env python3
"""Évalue la force d'un mot de passe et donne des suggestions (stdlib re)."""
import argparse
import re
import sys

COMMON_PATTERNS = [
    "123456", "password", "azerty", "qwerty", "letmein", "admin",
    "welcome", "abc123", "iloveyou", "111111", "000000", "motdepasse",
]


def evaluate(password: str) -> tuple[int, list[str]]:
    score = 0
    suggestions = []

    if len(password) >= 12:
        score += 2
    elif len(password) >= 8:
        score += 1
    else:
        suggestions.append("Utiliser au moins 8 caractères (12+ recommandé).")

    if re.search(r"[a-z]", password):
        score += 1
    else:
        suggestions.append("Ajouter des lettres minuscules.")

    if re.search(r"[A-Z]", password):
        score += 1
    else:
        suggestions.append("Ajouter des lettres majuscules.")

    if re.search(r"\d", password):
        score += 1
    else:
        suggestions.append("Ajouter des chiffres.")

    if re.search(r"[^A-Za-z0-9]", password):
        score += 1
    else:
        suggestions.append("Ajouter des symboles (!@#$...).")

    lowered = password.lower()
    if any(pattern in lowered for pattern in COMMON_PATTERNS):
        score -= 2
        suggestions.append("Éviter les motifs courants (123456, password, azerty...).")

    if re.search(r"(.)\1{2,}", password):
        score -= 1
        suggestions.append("Éviter les répétitions de caractères (aaa, 111...).")

    score = max(0, min(score, 6))
    return score, suggestions


def label(score: int) -> str:
    if score <= 1:
        return "Très faible"
    if score <= 3:
        return "Faible"
    if score <= 4:
        return "Moyen"
    if score <= 5:
        return "Fort"
    return "Très fort"


def main() -> None:
    parser = argparse.ArgumentParser(description="Évalue la force d'un mot de passe.")
    parser.add_argument("password", help="Mot de passe à évaluer")
    args = parser.parse_args()

    score, suggestions = evaluate(args.password)
    print(f"Score: {score}/6 ({label(score)})")
    if suggestions:
        print("Suggestions:")
        for s in suggestions:
            print(f"  - {s}")
    else:
        print("Aucune suggestion, ce mot de passe est robuste.")

    if score <= 2:
        sys.exit(1)


if __name__ == "__main__":
    main()
