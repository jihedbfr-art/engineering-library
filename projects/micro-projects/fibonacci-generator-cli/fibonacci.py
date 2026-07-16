#!/usr/bin/env python3
"""Génère les N premiers termes de Fibonacci, ou calcule un terme précis (itératif, O(n))."""
import argparse
import sys


def fibonacci_sequence(count: int) -> list[int]:
    if count <= 0:
        return []
    sequence = [0, 1]
    while len(sequence) < count:
        sequence.append(sequence[-1] + sequence[-2])
    return sequence[:count]


def fibonacci_nth(n: int) -> int:
    if n < 0:
        raise ValueError("n doit être positif ou nul")
    if n == 0:
        return 0
    a, b = 0, 1
    for _ in range(n - 1):
        a, b = b, a + b
    return b


def main() -> None:
    parser = argparse.ArgumentParser(description="Génère une séquence de Fibonacci ou un terme précis.")
    parser.add_argument("-n", "--count", type=int, default=10, help="Nombre de termes à générer (défaut: 10)")
    parser.add_argument("--term", type=int, default=None, help="Calcule uniquement le terme n (indexé à partir de 0)")
    args = parser.parse_args()

    if args.term is not None:
        try:
            print(fibonacci_nth(args.term))
        except ValueError as e:
            print(f"Erreur: {e}", file=sys.stderr)
            sys.exit(1)
    else:
        if args.count < 0:
            print("Erreur: count doit être positif ou nul.", file=sys.stderr)
            sys.exit(1)
        print(" ".join(str(x) for x in fibonacci_sequence(args.count)))


if __name__ == "__main__":
    main()
