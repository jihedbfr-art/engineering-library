#!/usr/bin/env python3
"""Convertit un XML simple en JSON."""
import argparse
import json
import sys
import xml.etree.ElementTree as ET


def element_to_dict(elem: ET.Element) -> dict | str:
    children = list(elem)
    result: dict = {}

    if elem.attrib:
        result["@attributes"] = dict(elem.attrib)

    if not children:
        text = (elem.text or "").strip()
        if not result:
            return text
        if text:
            result["#text"] = text
        return result

    child_data: dict = {}
    for child in children:
        value = element_to_dict(child)
        if child.tag in child_data:
            existing = child_data[child.tag]
            if isinstance(existing, list):
                existing.append(value)
            else:
                child_data[child.tag] = [existing, value]
        else:
            child_data[child.tag] = value

    result.update(child_data)
    return result


def xml_to_json(xml_text: str) -> dict:
    root = ET.fromstring(xml_text)
    return {root.tag: element_to_dict(root)}


def main():
    parser = argparse.ArgumentParser(description="Convertit un fichier XML simple en JSON.")
    parser.add_argument("input", help="Fichier XML d'entrée")
    parser.add_argument("-o", "--output", help="Fichier JSON de sortie (défaut: stdout)")
    parser.add_argument("--indent", type=int, default=2, help="Indentation du JSON (défaut: 2)")
    args = parser.parse_args()

    try:
        with open(args.input, "r", encoding="utf-8") as f:
            xml_text = f.read()
        data = xml_to_json(xml_text)
    except (ET.ParseError, FileNotFoundError) as exc:
        parser.error(str(exc))
        return

    output = json.dumps(data, indent=args.indent, ensure_ascii=False)

    if args.output:
        with open(args.output, "w", encoding="utf-8") as out:
            out.write(output + "\n")
    else:
        print(output)


if __name__ == "__main__":
    main()
