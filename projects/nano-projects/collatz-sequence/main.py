def collatz(n: int) -> list[int]:
    seq = [n]
    while n != 1:
        n = n // 2 if n % 2 == 0 else 3 * n + 1
        seq.append(n)
    return seq


if __name__ == "__main__":
    n = int(input("Nombre de départ : "))
    seq = collatz(n)
    print(f"{seq} ({len(seq) - 1} étapes)")
