SMALL_WORDS = {"a", "an", "the", "and", "but", "or", "for", "nor", "on", "at", "to", "from", "by", "of", "in"}


def title_case(text: str) -> str:
    words = text.split()
    result = []
    for i, word in enumerate(words):
        lower = word.lower()
        if i != 0 and i != len(words) - 1 and lower in SMALL_WORDS:
            result.append(lower)
        else:
            result.append(lower.capitalize())
    return " ".join(result)


if __name__ == "__main__":
    text = input("Texte a convertir en Title Case : ")
    print(title_case(text))
