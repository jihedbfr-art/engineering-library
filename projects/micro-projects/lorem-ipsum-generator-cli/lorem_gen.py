#!/usr/bin/env python3
"""Génère du faux texte latin (lorem ipsum) : mots, phrases ou paragraphes."""
import argparse
import random

WORDS = [
    "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
    "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore",
    "magna", "aliqua", "enim", "ad", "minim", "veniam", "quis", "nostrud",
    "exercitation", "ullamco", "laboris", "nisi", "aliquip", "ex", "ea", "commodo",
    "consequat", "duis", "aute", "irure", "in", "reprehenderit", "voluptate",
    "velit", "esse", "cillum", "eu", "fugiat", "nulla", "pariatur", "excepteur",
    "sint", "occaecat", "cupidatat", "non", "proident", "sunt", "culpa", "qui",
    "officia", "deserunt", "mollit", "anim", "id", "est", "laborum",
]


def make_sentence(min_words: int = 6, max_words: int = 14) -> str:
    n = random.randint(min_words, max_words)
    words = random.choices(WORDS, k=n)
    sentence = " ".join(words)
    return sentence[0].upper() + sentence[1:] + "."


def make_paragraph(sentences: int = 5) -> str:
    return " ".join(make_sentence() for _ in range(sentences))


def make_words(count: int) -> str:
    return " ".join(random.choices(WORDS, k=count))


def main():
    parser = argparse.ArgumentParser(description="Génère du texte lorem ipsum.")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("-p", "--paragraphs", type=int, help="Nombre de paragraphes à générer")
    group.add_argument("-w", "--words", type=int, help="Nombre de mots à générer")
    parser.add_argument("--sentences-per-paragraph", type=int, default=5, help="Phrases par paragraphe (défaut: 5)")
    parser.add_argument("--seed", type=int, help="Graine aléatoire pour un résultat reproductible")
    args = parser.parse_args()

    if args.seed is not None:
        random.seed(args.seed)

    if args.words:
        print(make_words(args.words))
        return

    paragraphs = args.paragraphs or 3
    for i in range(paragraphs):
        print(make_paragraph(args.sentences_per_paragraph))
        if i < paragraphs - 1:
            print()


if __name__ == "__main__":
    main()
