def word_wrap(text: str, width: int) -> str:
    lines = []
    current = []
    current_len = 0

    for word in text.split():
        extra = len(word) if not current else len(word) + 1
        if current_len + extra > width and current:
            lines.append(" ".join(current))
            current = [word]
            current_len = len(word)
        else:
            current.append(word)
            current_len += extra

    if current:
        lines.append(" ".join(current))

    return "\n".join(lines)


if __name__ == "__main__":
    text = input("Texte a formater : ")
    width = int(input("Largeur max (caracteres) : "))
    print(word_wrap(text, width))
