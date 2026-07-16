#!/usr/bin/env python3
"""Génère un sitemap.xml valide à partir d'une liste d'URLs (une par ligne)."""
import argparse
import sys
import xml.etree.ElementTree as ET
from datetime import date

NS = "http://www.sitemaps.org/schemas/sitemap/0.9"


def build_sitemap(urls: list[str], changefreq: str | None, priority: str | None) -> ET.ElementTree:
    ET.register_namespace("", NS)
    urlset = ET.Element("urlset", {"xmlns": NS})

    for raw in urls:
        url = raw.strip()
        if not url:
            continue
        url_el = ET.SubElement(urlset, "url")
        loc = ET.SubElement(url_el, "loc")
        loc.text = url
        lastmod = ET.SubElement(url_el, "lastmod")
        lastmod.text = date.today().isoformat()
        if changefreq:
            cf = ET.SubElement(url_el, "changefreq")
            cf.text = changefreq
        if priority:
            pr = ET.SubElement(url_el, "priority")
            pr.text = priority

    return ET.ElementTree(urlset)


def main() -> None:
    parser = argparse.ArgumentParser(description="Génère un sitemap.xml à partir d'une liste d'URLs.")
    parser.add_argument("urlfile", help="Fichier texte, une URL par ligne")
    parser.add_argument("-o", "--output", default="sitemap.xml", help="Fichier de sortie (défaut: sitemap.xml)")
    parser.add_argument("--changefreq", default=None,
                         choices=["always", "hourly", "daily", "weekly", "monthly", "yearly", "never"],
                         help="Fréquence de changement (optionnelle)")
    parser.add_argument("--priority", default=None, help="Priorité 0.0-1.0 (optionnelle)")
    args = parser.parse_args()

    try:
        with open(args.urlfile, encoding="utf-8") as f:
            urls = [line.strip() for line in f if line.strip() and not line.startswith("#")]
    except OSError as e:
        print(f"Erreur: {e}", file=sys.stderr)
        sys.exit(1)

    if not urls:
        print("Erreur: aucune URL trouvée dans le fichier.", file=sys.stderr)
        sys.exit(1)

    invalid = [u for u in urls if not (u.startswith("http://") or u.startswith("https://"))]
    if invalid:
        print(f"Erreur: URLs invalides (doivent commencer par http:// ou https://): {invalid}", file=sys.stderr)
        sys.exit(1)

    tree = build_sitemap(urls, args.changefreq, args.priority)
    tree.write(args.output, encoding="UTF-8", xml_declaration=True)
    print(f"Sitemap généré: {args.output} ({len(urls)} URLs)")


if __name__ == "__main__":
    main()
