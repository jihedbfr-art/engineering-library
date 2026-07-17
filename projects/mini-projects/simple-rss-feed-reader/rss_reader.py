import argparse
import xml.etree.ElementTree as ET


def parse_rss(xml_content: str) -> list[dict]:
    root = ET.fromstring(xml_content)
    items = []
    for item in root.findall(".//item"):
        items.append({
            "title": item.findtext("title", ""),
            "link": item.findtext("link", ""),
            "pubDate": item.findtext("pubDate", ""),
        })
    return items


def main():
    parser = argparse.ArgumentParser(description="Parse un fichier RSS/XML local et affiche les articles")
    parser.add_argument("file")
    args = parser.parse_args()

    with open(args.file, encoding="utf-8") as f:
        for item in parse_rss(f.read()):
            print(f"- {item['title']} ({item['pubDate']})")
            print(f"  {item['link']}")


if __name__ == "__main__":
    main()
