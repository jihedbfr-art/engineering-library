def reverse_string(s: str) -> str:
    return s[::-1]


if __name__ == "__main__":
    s = input("Texte : ")
    print(reverse_string(s))
