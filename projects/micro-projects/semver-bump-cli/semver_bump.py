import argparse
import re


def bump(version: str, part: str) -> str:
    match = re.match(r"^(\d+)\.(\d+)\.(\d+)$", version)
    if not match:
        raise ValueError("Format attendu: MAJOR.MINOR.PATCH")
    major, minor, patch = (int(x) for x in match.groups())
    if part == "major":
        return f"{major + 1}.0.0"
    if part == "minor":
        return f"{major}.{minor + 1}.0"
    return f"{major}.{minor}.{patch + 1}"


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Incremente une version semver")
    parser.add_argument("version")
    parser.add_argument("part", choices=["major", "minor", "patch"])
    args = parser.parse_args()
    print(bump(args.version, args.part))
