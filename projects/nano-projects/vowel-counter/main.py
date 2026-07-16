def count_vowels(text: str) -> int:
    return sum(1 for c in text.lower() if c in "aeiouyàâéèêëîïôöùûü")


if __name__ == "__main__":
    text = input("Texte : ")
    print(f"Voyelles : {count_vowels(text)}")
