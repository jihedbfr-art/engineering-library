import argparse


def parse_env(path: str) -> dict:
    result = {}
    for line in open(path):
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, _, value = line.partition("=")
            result[key.strip()] = value.strip()
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Diff de deux fichiers .env (cles seulement)")
    parser.add_argument("file_a")
    parser.add_argument("file_b")
    args = parser.parse_args()

    a, b = parse_env(args.file_a), parse_env(args.file_b)
    only_a = set(a) - set(b)
    only_b = set(b) - set(a)
    if only_a:
        print(f"Present seulement dans {args.file_a}:", ", ".join(sorted(only_a)))
    if only_b:
        print(f"Present seulement dans {args.file_b}:", ", ".join(sorted(only_b)))
    if not only_a and not only_b:
        print("Memes cles des deux cotes.")
