from collections import Counter


def histogram(text: str) -> str:
    counts = Counter(c for c in text.lower() if c.isalpha())
    lines = [f"{c}: {'#' * n} ({n})" for c, n in sorted(counts.items())]
    return "\n".join(lines)


if __name__ == "__main__":
    text = input("Texte : ")
    print(histogram(text))
