import argparse


def main():
    parser = argparse.ArgumentParser(description="Supprime les lignes dupliquees d'un fichier (garde l'ordre d'apparition)")
    parser.add_argument("file")
    parser.add_argument("-o", "--output", help="fichier de sortie (defaut: stdout)")
    args = parser.parse_args()

    with open(args.file, encoding="utf-8") as f:
        seen = dict.fromkeys(f.readlines())

    output = "".join(seen)
    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(output)
    else:
        print(output, end="")


if __name__ == "__main__":
    main()
