import random


def roll_dice(n: int, faces: int) -> list[int]:
    return [random.randint(1, faces) for _ in range(n)]


if __name__ == "__main__":
    n = int(input("Nombre de dés : "))
    faces = int(input("Nombre de faces : "))
    rolls = roll_dice(n, faces)
    print(f"{rolls} (total : {sum(rolls)})")
