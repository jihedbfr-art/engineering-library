import random


def shuffle_list(items: list) -> list:
    shuffled = items.copy()
    random.shuffle(shuffled)
    return shuffled


if __name__ == "__main__":
    example = [1, 2, 3, 4, 5, 6, 7, 8]
    print(f"{example} -> {shuffle_list(example)}")
