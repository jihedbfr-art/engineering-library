#!/usr/bin/env python3
"""Calcule le score de lisibilite Flesch Reading Ease et le niveau scolaire Flesch-Kincaid
d'un texte anglais.

Le comptage de syllabes est heuristique (compte les groupes de voyelles), pas un vrai
decoupage phonetique — suffisant pour comparer deux textes entre eux, pas pour un score
publie tel quel dans un rapport officiel.
"""
import argparse
import re
import sys

VOWELS = "aeiouy"


def count_syllables(word: str) -> int:
    word = word.lower().strip(".,;:!?\"'()")
    if not word:
        return 0

    syllables = 0
    previous_was_vowel = False
    for char in word:
        is_vowel = char in VOWELS
        if is_vowel and not previous_was_vowel:
            syllables += 1
        previous_was_vowel = is_vowel

    if word.endswith("e") and syllables > 1:
        syllables -= 1

    return max(syllables, 1)


def analyze(text: str) -> dict:
    sentences = [s for s in re.split(r"[.!?]+", text) if s.strip()]
    words = re.findall(r"[A-Za-z']+", text)

    sentence_count = max(len(sentences), 1)
    word_count = max(len(words), 1)
    syllable_count = sum(count_syllables(w) for w in words)

    words_per_sentence = word_count / sentence_count
    syllables_per_word = syllable_count / word_count

    flesch_reading_ease = 206.835 - 1.015 * words_per_sentence - 84.6 * syllables_per_word
    flesch_kincaid_grade = 0.39 * words_per_sentence + 11.8 * syllables_per_word - 15.59

    return {
        "sentences": sentence_count,
        "words": word_count,
        "syllables": syllable_count,
        "flesch_reading_ease": round(flesch_reading_ease, 1),
        "flesch_kincaid_grade": round(flesch_kincaid_grade, 1),
    }


def interpret(score: float) -> str:
    if score >= 90:
        return "tres facile (CE1-CE2)"
    if score >= 70:
        return "facile (college)"
    if score >= 50:
        return "moyen (lycee)"
    if score >= 30:
        return "difficile (universitaire)"
    return "tres difficile (specialiste)"


def main():
    parser = argparse.ArgumentParser(description="Score de lisibilite Flesch d'un texte anglais.")
    parser.add_argument("file", help="Fichier texte, ou '-' pour lire depuis stdin")
    args = parser.parse_args()

    text = sys.stdin.read() if args.file == "-" else open(args.file, encoding="utf-8").read()

    result = analyze(text)
    print(f"Phrases         : {result['sentences']}")
    print(f"Mots            : {result['words']}")
    print(f"Syllabes (est.) : {result['syllables']}")
    print(f"Flesch Reading Ease : {result['flesch_reading_ease']} ({interpret(result['flesch_reading_ease'])})")
    print(f"Flesch-Kincaid Grade Level : {result['flesch_kincaid_grade']}")


if __name__ == "__main__":
    main()
