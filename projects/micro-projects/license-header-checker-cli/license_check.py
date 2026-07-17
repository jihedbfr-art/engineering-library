import argparse
import pathlib


def has_header(path: pathlib.Path, expected: str, lines_to_check: int = 5) -> bool:
    with open(path, encoding="utf-8", errors="ignore") as f:
        head = "".join(next(f) for _ in range(lines_to_check) if f)
    return expected in head


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Verifie qu'un header de licence est present dans les fichiers source")
    parser.add_argument("directory")
    parser.add_argument("--header", default="Copyright", help="texte attendu dans les premieres lignes")
    parser.add_argument("--ext", default=".java", help="extension a verifier")
    args = parser.parse_args()

    missing = []
    for path in pathlib.Path(args.directory).rglob(f"*{args.ext}"):
        if not has_header(path, args.header):
            missing.append(str(path))

    if missing:
        print(f"{len(missing)} fichier(s) sans header '{args.header}':")
        for m in missing:
            print(f"  - {m}")
    else:
        print("Tous les fichiers ont le header attendu.")
