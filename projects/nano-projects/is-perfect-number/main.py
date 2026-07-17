def is_perfect(n: int) -> bool:
    return n > 0 and sum(d for d in range(1, n) if n % d == 0) == n


if __name__ == "__main__":
    n = int(input("Nombre : "))
    print(f"{n} {'est' if is_perfect(n) else 'n est pas'} parfait")
