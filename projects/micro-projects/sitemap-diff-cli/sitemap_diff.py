import argparse
import xml.etree.ElementTree as ET


def extract_urls(path: str) -> set[str]:
    ns = {"sm": "http://www.sitemaps.org/schemas/sitemap/0.9"}
    tree = ET.parse(path)
    return {loc.text for loc in tree.getroot().findall(".//sm:loc", ns)}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Diff de deux sitemaps XML")
    parser.add_argument("old")
    parser.add_argument("new")
    args = parser.parse_args()

    old_urls, new_urls = extract_urls(args.old), extract_urls(args.new)
    for url in sorted(new_urls - old_urls):
        print(f"+ {url}")
    for url in sorted(old_urls - new_urls):
        print(f"- {url}")
