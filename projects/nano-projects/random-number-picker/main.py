import random


def pick_number(low: int, high: int) -> int:
    return random.randint(low, high)


if __name__ == "__main__":
    low = int(input("Borne min : "))
    high = int(input("Borne max : "))
    print(pick_number(low, high))
