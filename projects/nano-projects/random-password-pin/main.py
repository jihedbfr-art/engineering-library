import secrets


def generate_pin(length: int = 6) -> str:
    return "".join(str(secrets.randbelow(10)) for _ in range(length))


if __name__ == "__main__":
    length = int(input("Longueur du PIN (defaut 6) : ") or 6)
    print(generate_pin(length))
