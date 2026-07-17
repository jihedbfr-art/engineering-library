VALUES = {"I": 1, "V": 5, "X": 10, "L": 50, "C": 100, "D": 500, "M": 1000}


def roman_to_decimal(s: str) -> int:
    total = 0
    for i, c in enumerate(s):
        v = VALUES[c]
        if i + 1 < len(s) and v < VALUES[s[i + 1]]:
            total -= v
        else:
            total += v
    return total


if __name__ == "__main__":
    s = input("Chiffre romain : ")
    print(roman_to_decimal(s.upper()))
