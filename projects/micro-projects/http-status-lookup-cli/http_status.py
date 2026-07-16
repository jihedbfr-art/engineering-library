#!/usr/bin/env python3
"""Table des codes de statut HTTP : recherche par code ou par mot-clé."""
import argparse

STATUS_CODES = {
    100: "Continue", 101: "Switching Protocols", 102: "Processing", 103: "Early Hints",
    200: "OK", 201: "Created", 202: "Accepted", 203: "Non-Authoritative Information",
    204: "No Content", 205: "Reset Content", 206: "Partial Content",
    300: "Multiple Choices", 301: "Moved Permanently", 302: "Found", 303: "See Other",
    304: "Not Modified", 307: "Temporary Redirect", 308: "Permanent Redirect",
    400: "Bad Request", 401: "Unauthorized", 402: "Payment Required", 403: "Forbidden",
    404: "Not Found", 405: "Method Not Allowed", 406: "Not Acceptable",
    407: "Proxy Authentication Required", 408: "Request Timeout", 409: "Conflict",
    410: "Gone", 411: "Length Required", 412: "Precondition Failed",
    413: "Payload Too Large", 414: "URI Too Long", 415: "Unsupported Media Type",
    416: "Range Not Satisfiable", 417: "Expectation Failed", 418: "I'm a Teapot",
    422: "Unprocessable Entity", 423: "Locked", 425: "Too Early", 426: "Upgrade Required",
    428: "Precondition Required", 429: "Too Many Requests",
    431: "Request Header Fields Too Large", 451: "Unavailable For Legal Reasons",
    500: "Internal Server Error", 501: "Not Implemented", 502: "Bad Gateway",
    503: "Service Unavailable", 504: "Gateway Timeout", 505: "HTTP Version Not Supported",
    507: "Insufficient Storage", 508: "Loop Detected", 510: "Not Extended",
    511: "Network Authentication Required",
}


def lookup_code(code: int) -> None:
    if code in STATUS_CODES:
        print(f"{code} {STATUS_CODES[code]}")
    else:
        print(f"Code {code} inconnu dans la table.")


def search_keyword(keyword: str) -> None:
    keyword_lower = keyword.lower()
    matches = [(c, d) for c, d in STATUS_CODES.items() if keyword_lower in d.lower()]
    if not matches:
        print(f"Aucun résultat pour {keyword!r}.")
        return
    for code, description in sorted(matches):
        print(f"{code} {description}")


def main():
    parser = argparse.ArgumentParser(description="Recherche dans la table des codes HTTP.")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("-c", "--code", type=int, help="Recherche par code exact, ex: 404")
    group.add_argument("-k", "--keyword", help="Recherche par mot-clé dans la description, ex: 'not found'")
    args = parser.parse_args()

    if args.code is not None:
        lookup_code(args.code)
    else:
        search_keyword(args.keyword)


if __name__ == "__main__":
    main()
