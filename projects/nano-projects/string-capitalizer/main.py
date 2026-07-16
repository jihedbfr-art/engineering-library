def capitalize_words(text: str) -> str:
    return " ".join(word.capitalize() for word in text.split(" "))


if __name__ == "__main__":
    text = input("Phrase : ")
    print(capitalize_words(text))
