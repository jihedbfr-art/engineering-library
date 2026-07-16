#!/usr/bin/env python3
"""Calcule le hash (MD5/SHA1/SHA256/SHA512) d'un fichier ou d'une chaine de texte."""
import argparse
import hashlib
import sys

ALGOS = {
    "md5": hashlib.md5,
    "sha1": hashlib.sha1,
    "sha256": hashlib.sha256,
    "sha512": hashlib.sha512,
}


def hash_file(path, algo_name, chunk_size=65536):
    h = ALGOS[algo_name]()
    with open(path, "rb") as f:
        while True:
            chunk = f.read(chunk_size)
            if not chunk:
                break
            h.update(chunk)
    return h.hexdigest()


def hash_text(text, algo_name):
    h = ALGOS[algo_name]()
    h.update(text.encode("utf-8"))
    return h.hexdigest()


def main():
    parser = argparse.ArgumentParser(description="Calcule un hash MD5/SHA1/SHA256/SHA512.")
    parser.add_argument("--algo", choices=sorted(ALGOS.keys()), default="sha256",
                         help="Algorithme de hash (defaut: sha256)")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--file", help="Chemin du fichier a hasher")
    group.add_argument("--text", help="Chaine de texte a hasher")
    args = parser.parse_args()

    try:
        if args.file:
            digest = hash_file(args.file, args.algo)
        else:
            digest = hash_text(args.text, args.algo)
    except OSError as e:
        print(f"Erreur: {e}", file=sys.stderr)
        sys.exit(1)

    print(f"{args.algo}: {digest}")


if __name__ == "__main__":
    main()
