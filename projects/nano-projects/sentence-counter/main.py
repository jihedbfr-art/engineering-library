import re


def count_sentences(text: str) -> int:
    # Coupe sur . ! ? mais pas sur une abreviation courante suivie d'une majuscule
    # (heuristique simple, pas un vrai parseur linguistique)
    parts = re.split(r'(?<!\bM)(?<!\bMr)(?<!\bMme)[.!?]+', text)
    return len([p for p in parts if p.strip()])


if __name__ == "__main__":
    text = input("Texte a analyser : ")
    print(f"Nombre de phrases (estimation) : {count_sentences(text)}")
