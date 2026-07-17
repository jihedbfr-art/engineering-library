import random

FIRST = ["Alex", "Sam", "Nour", "Lea", "Youssef", "Mia", "Karim", "Zoe"]
LAST = ["Martin", "Bernard", "Trabelsi", "Haddad", "Ferrari", "Kowalski"]


def random_name() -> str:
    return f"{random.choice(FIRST)} {random.choice(LAST)}"


if __name__ == "__main__":
    print(random_name())
