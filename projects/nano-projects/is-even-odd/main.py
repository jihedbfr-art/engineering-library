def is_even(n: int) -> bool:
    return n % 2 == 0


if __name__ == "__main__":
    n = int(input("Nombre : "))
    print(f"{n} est {'pair' if is_even(n) else 'impair'}")
