#!/usr/bin/env python3
"""Encode/décode en base64 un fichier ou un texte."""
import argparse
import base64
import sys


def main() -> None:
    parser = argparse.ArgumentParser(description="Encode ou décode du base64 (fichier ou texte).")
    mode_group = parser.add_mutually_exclusive_group(required=True)
    mode_group.add_argument("--encode", action="store_true", help="Mode encodage")
    mode_group.add_argument("--decode", action="store_true", help="Mode décodage")

    input_group = parser.add_mutually_exclusive_group(required=True)
    input_group.add_argument("-t", "--text", help="Texte à encoder/décoder directement")
    input_group.add_argument("-f", "--file", help="Fichier à encoder/décoder")

    parser.add_argument("-o", "--output", help="Fichier de sortie (défaut: stdout)")
    args = parser.parse_args()

    if args.file:
        mode = "rb" if args.encode else "r"
        try:
            with open(args.file, mode, encoding=None if args.encode else "utf-8") as f:
                raw = f.read()
        except FileNotFoundError:
            print(f"Erreur: fichier introuvable - {args.file}", file=sys.stderr)
            sys.exit(1)
    else:
        raw = args.text

    try:
        if args.encode:
            data_bytes = raw if isinstance(raw, bytes) else raw.encode("utf-8")
            result = base64.b64encode(data_bytes).decode("ascii")
        else:
            data_str = raw if isinstance(raw, str) else raw.decode("utf-8")
            decoded_bytes = base64.b64decode(data_str)
            try:
                result = decoded_bytes.decode("utf-8")
            except UnicodeDecodeError:
                # Contenu binaire décodé : on écrit en binaire si -o est fourni
                if args.output:
                    with open(args.output, "wb") as f:
                        f.write(decoded_bytes)
                    print(f"Contenu binaire décodé et écrit dans {args.output}")
                    return
                print("Erreur: le contenu décodé est binaire, utilisez -o pour l'écrire dans un fichier.", file=sys.stderr)
                sys.exit(1)
    except Exception as e:
        print(f"Erreur: {e}", file=sys.stderr)
        sys.exit(1)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(result)
    else:
        print(result)


if __name__ == "__main__":
    main()
