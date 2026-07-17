def longest_word(text: str) -> str:
    return max(text.split(), key=len)


if __name__ == "__main__":
    text = input("Texte : ")
    print(longest_word(text))
