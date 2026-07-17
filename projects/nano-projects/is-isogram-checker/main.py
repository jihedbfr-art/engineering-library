def is_isogram(word: str) -> bool:
    letters = [c for c in word.lower() if c.isalpha()]
    return len(letters) == len(set(letters))


if __name__ == "__main__":
    word = input("Mot : ")
    print("Isogramme" if is_isogram(word) else "Pas un isogramme")
