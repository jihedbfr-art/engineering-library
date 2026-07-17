import argparse
import re

PATTERN = re.compile(r"^(feat|fix|docs|style|refactor|test|chore|perf)(\([\w-]+\))?: .{1,72}$")


def lint(message: str) -> list[str]:
    errors = []
    first_line = message.splitlines()[0] if message else ""
    if not PATTERN.match(first_line):
        errors.append("La premiere ligne doit suivre 'type(scope?): description' (Conventional Commits)")
    if len(first_line) > 72:
        errors.append("Premiere ligne trop longue (> 72 caracteres)")
    return errors


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Verifie un message de commit (Conventional Commits)")
    parser.add_argument("message_file")
    args = parser.parse_args()
    with open(args.message_file) as f:
        message = f.read()
    errors = lint(message)
    if errors:
        for e in errors:
            print(f"ERREUR: {e}")
    else:
        print("Message de commit conforme.")
