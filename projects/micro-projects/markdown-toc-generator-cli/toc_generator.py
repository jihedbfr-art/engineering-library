import argparse
import re


def generate_toc(text: str) -> str:
    lines = []
    for match in re.finditer(r"^(#{1,6})\s+(.+)$", text, re.MULTILINE):
        level = len(match.group(1))
        title = match.group(2).strip()
        anchor = re.sub(r"[^\w\s-]", "", title).lower().replace(" ", "-")
        lines.append(f"{'  ' * (level - 1)}- [{title}](#{anchor})")
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="Genere une table des matieres depuis un fichier Markdown")
    parser.add_argument("file")
    args = parser.parse_args()

    with open(args.file, encoding="utf-8") as f:
        print(generate_toc(f.read()))


if __name__ == "__main__":
    main()
