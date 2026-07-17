import argparse
import json


def diff(a, b, path=""):
    changes = []
    if isinstance(a, dict) and isinstance(b, dict):
        for key in sorted(set(a) | set(b)):
            p = f"{path}.{key}" if path else key
            if key not in a:
                changes.append(f"+ {p} = {b[key]!r}")
            elif key not in b:
                changes.append(f"- {p} = {a[key]!r}")
            else:
                changes.extend(diff(a[key], b[key], p))
    elif a != b:
        changes.append(f"~ {path}: {a!r} -> {b!r}")
    return changes


def main():
    parser = argparse.ArgumentParser(description="Diff structurel entre deux fichiers JSON")
    parser.add_argument("file_a")
    parser.add_argument("file_b")
    args = parser.parse_args()

    with open(args.file_a) as f:
        a = json.load(f)
    with open(args.file_b) as f:
        b = json.load(f)

    changes = diff(a, b)
    print("\n".join(changes) if changes else "Aucune difference")


if __name__ == "__main__":
    main()
