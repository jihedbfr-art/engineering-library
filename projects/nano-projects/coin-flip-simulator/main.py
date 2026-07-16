import random


def flip_coins(n: int) -> dict:
    counts = {"pile": 0, "face": 0}
    for _ in range(n):
        counts[random.choice(["pile", "face"])] += 1
    return counts


if __name__ == "__main__":
    n = int(input("Nombre de lancers : "))
    counts = flip_coins(n)
    print(f"Pile : {counts['pile']} | Face : {counts['face']}")
