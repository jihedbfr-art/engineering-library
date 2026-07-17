import argparse
import pathlib
import re


def markdown_to_html(md: str) -> str:
    html = md
    html = re.sub(r"^# (.*)$", r"<h1>\1</h1>", html, flags=re.MULTILINE)
    html = re.sub(r"^## (.*)$", r"<h2>\1</h2>", html, flags=re.MULTILINE)
    html = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", html)
    html = re.sub(r"\n\n", "</p><p>", html)
    return f"<p>{html}</p>"


def main():
    parser = argparse.ArgumentParser(description="Genere un site statique a partir de fichiers Markdown")
    parser.add_argument("source_dir", help="dossier contenant les .md")
    parser.add_argument("output_dir", help="dossier de sortie HTML")
    args = parser.parse_args()

    src = pathlib.Path(args.source_dir)
    out = pathlib.Path(args.output_dir)
    out.mkdir(parents=True, exist_ok=True)

    posts = []
    for md_file in src.glob("*.md"):
        content = md_file.read_text(encoding="utf-8")
        title = content.splitlines()[0].lstrip("# ")
        html_body = markdown_to_html(content)
        page = f"<!DOCTYPE html><html><head><meta charset='UTF-8'><title>{title}</title></head><body>{html_body}</body></html>"
        out_file = out / (md_file.stem + ".html")
        out_file.write_text(page, encoding="utf-8")
        posts.append((title, out_file.name))

    index = "<h1>Blog</h1><ul>" + "".join(f'<li><a href="{f}">{t}</a></li>' for t, f in posts) + "</ul>"
    (out / "index.html").write_text(index, encoding="utf-8")
    print(f"{len(posts)} article(s) generes dans {out}")


if __name__ == "__main__":
    main()
