#!/usr/bin/env python3
"""Convertit un sous-ensemble de Markdown en HTML (stdlib uniquement, pas de lib externe).

Supporte : titres (#..######), gras (**texte**), italique (*texte*),
listes à puces (- item), liens ([texte](url)), paragraphes.
"""
import argparse
import re
import sys


def escape_html(text: str) -> str:
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    )


def inline_format(text: str) -> str:
    text = escape_html(text)
    # Liens [texte](url)
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r'<a href="\2">\1</a>', text)
    # Gras **texte**
    text = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", text)
    # Italique *texte* (après le gras pour ne pas confondre les deux)
    text = re.sub(r"\*([^*]+)\*", r"<em>\1</em>", text)
    return text


def convert(markdown_text: str) -> str:
    lines = markdown_text.splitlines()
    html_lines = []
    paragraph_buffer = []
    in_list = False

    def flush_paragraph():
        if paragraph_buffer:
            joined = " ".join(paragraph_buffer)
            html_lines.append(f"<p>{inline_format(joined)}</p>")
            paragraph_buffer.clear()

    def close_list():
        nonlocal in_list
        if in_list:
            html_lines.append("</ul>")
            in_list = False

    for raw_line in lines:
        line = raw_line.rstrip()

        if not line.strip():
            flush_paragraph()
            close_list()
            continue

        heading_match = re.match(r"^(#{1,6})\s+(.*)$", line)
        if heading_match:
            flush_paragraph()
            close_list()
            level = len(heading_match.group(1))
            content = inline_format(heading_match.group(2).strip())
            html_lines.append(f"<h{level}>{content}</h{level}>")
            continue

        list_match = re.match(r"^-\s+(.*)$", line)
        if list_match:
            flush_paragraph()
            if not in_list:
                html_lines.append("<ul>")
                in_list = True
            html_lines.append(f"<li>{inline_format(list_match.group(1).strip())}</li>")
            continue

        close_list()
        paragraph_buffer.append(line.strip())

    flush_paragraph()
    close_list()

    return "\n".join(html_lines)


def main() -> None:
    parser = argparse.ArgumentParser(description="Convertit un sous-ensemble de Markdown en HTML.")
    parser.add_argument("file", nargs="?", help="Fichier Markdown à lire (défaut: stdin)")
    parser.add_argument("-o", "--output", help="Fichier de sortie (défaut: stdout)")
    args = parser.parse_args()

    if args.file:
        with open(args.file, "r", encoding="utf-8") as f:
            source = f.read()
    else:
        source = sys.stdin.read()

    html = convert(source)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(html + "\n")
    else:
        print(html)


if __name__ == "__main__":
    main()
