import argparse
import re
import subprocess


CATEGORIES = {
    "feat": "Nouvelles fonctionnalites",
    "fix": "Corrections",
    "docs": "Documentation",
    "refactor": "Refactoring",
    "chore": "Divers",
}


def get_commits(since: str) -> list[str]:
    result = subprocess.run(
        ["git", "log", f"{since}..HEAD", "--pretty=format:%s"],
        capture_output=True, text=True, check=True,
    )
    return result.stdout.splitlines()


def categorize(commits: list[str]) -> dict[str, list[str]]:
    grouped = {cat: [] for cat in CATEGORIES}
    grouped["other"] = []
    for commit in commits:
        match = re.match(r"^(\w+)(\(.+\))?:\s*(.+)$", commit)
        if match and match.group(1) in CATEGORIES:
            grouped[match.group(1)].append(match.group(3))
        else:
            grouped["other"].append(commit)
    return grouped


def main():
    parser = argparse.ArgumentParser(description="Genere un CHANGELOG a partir des commits conventionnels depuis un tag/commit")
    parser.add_argument("since", help="tag ou commit de reference, ex: v1.0.0")
    args = parser.parse_args()

    grouped = categorize(get_commits(args.since))
    for key, label in CATEGORIES.items():
        if grouped[key]:
            print(f"\n## {label}")
            for msg in grouped[key]:
                print(f"- {msg}")
    if grouped["other"]:
        print("\n## Autres")
        for msg in grouped["other"]:
            print(f"- {msg}")


if __name__ == "__main__":
    main()
