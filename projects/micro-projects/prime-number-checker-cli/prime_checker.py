#!/usr/bin/env python3
"""Vérifie si un nombre est premier, ou liste les premiers jusqu'à N (crible d'Ératosthène)."""
import argparse
import math
import sys


def is_prime(n: int) -> bool:
    if n < 2:
        return False
    if n in (2, 3):
        return True
    if n % 2 == 0:
        return False
    for i in range(3, int(math.isqrt(n)) + 1, 2):
        if n % i == 0:
            return False
    return True


def sieve_of_eratosthenes(limit: int) -> list[int]:
    if limit < 2:
        return []
    is_p = [True] * (limit + 1)
    is_p[0] = is_p[1] = False
    for i in range(2, int(math.isqrt(limit)) + 1):
        if is_p[i]:
            for multiple in range(i * i, limit + 1, i):
                is_p[multiple] = False
    return [i for i, flag in enumerate(is_p) if flag]


def main() -> None:
    parser = argparse.ArgumentParser(description="Vérifie la primalité d'un nombre ou liste les premiers jusqu'à N.")
    parser.add_argument("n", type=int, help="Nombre à vérifier, ou limite si --list")
    parser.add_argument("-l", "--list", action="store_true", help="Lister tous les premiers jusqu'à N")
    args = parser.parse_args()

    if args.list:
        if args.n < 0:
            print("Erreur: la limite doit être positive.", file=sys.stderr)
            sys.exit(1)
        primes = sieve_of_eratosthenes(args.n)
        print(" ".join(str(p) for p in primes) if primes else "(aucun)")
    else:
        if is_prime(args.n):
            print(f"{args.n} est premier.")
        else:
            print(f"{args.n} n'est pas premier.")
            sys.exit(1)


if __name__ == "__main__":
    main()
