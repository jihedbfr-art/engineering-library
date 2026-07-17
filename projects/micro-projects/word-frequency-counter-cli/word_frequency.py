import argparse
import re
from collections import Counter


def main():
    parser = argparse.ArgumentParser(description="Compte la frequence des mots dans un texte")
    parser.add_argument("file", help="fichier texte")
    parser.add_argument("-n", "--top", type=int, default=10, help="nombre de mots a afficher")
    args = parser.parse_args()

    with open(args.file, encoding="utf-8") as f:
        words = re.findall(r"[a-zA-Zàâäéèêëïîôöùûüç]+", f.read().lower())

    for word, count in Counter(words).most_common(args.top):
        print(f"{word}: {count}")


if __name__ == "__main__":
    main()
