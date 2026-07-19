#!/usr/bin/env python3
"""Parse une URL et affiche ses composants + ses parametres de requete en JSON.

Utile pour deboguer un lien de tracking a rallonge ou verifier ce qu'un webhook
envoie reellement en query string, sans coller l'URL dans un outil en ligne tiers.
"""
import argparse
import json
import sys
from urllib.parse import urlparse, parse_qs


def main():
    parser = argparse.ArgumentParser(description="Parse une URL et ses query params.")
    parser.add_argument("url", help="URL complete a parser")
    parser.add_argument("--flatten", action="store_true",
                         help="Renvoie les valeurs uniques comme scalaires au lieu de listes "
                              "a un element (perd l'info si une cle est repetee)")
    args = parser.parse_args()

    parsed = urlparse(args.url)

    if not parsed.scheme or not parsed.netloc:
        print(f"Erreur: '{args.url}' ne ressemble pas a une URL absolue (schema/hote manquant).",
              file=sys.stderr)
        sys.exit(1)

    query = parse_qs(parsed.query, keep_blank_values=True)
    if args.flatten:
        query = {k: (v[0] if len(v) == 1 else v) for k, v in query.items()}

    output = {
        "scheme": parsed.scheme,
        "host": parsed.hostname,
        "port": parsed.port,
        "path": parsed.path or "/",
        "fragment": parsed.fragment or None,
        "query": query,
    }

    print(json.dumps(output, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
