#!/usr/bin/env python3
"""Valide une adresse IPv4/IPv6 et détecte son type (privée/publique/loopback/...)."""
import argparse
import ipaddress
import sys


def classify(ip_str: str) -> dict:
    try:
        ip = ipaddress.ip_address(ip_str)
    except ValueError:
        return {"valid": False}

    info = {
        "valid": True,
        "version": f"IPv{ip.version}",
        "loopback": ip.is_loopback,
        "private": ip.is_private,
        "public": not ip.is_private and not ip.is_loopback and not ip.is_link_local and not ip.is_reserved,
        "multicast": ip.is_multicast,
        "link_local": ip.is_link_local,
        "reserved": ip.is_reserved,
        "unspecified": ip.is_unspecified,
    }
    return info


def format_result(ip_str: str, info: dict) -> str:
    if not info["valid"]:
        return f"{ip_str} -> INVALIDE"

    tags = []
    if info["loopback"]:
        tags.append("loopback")
    if info["private"]:
        tags.append("privée")
    if info["public"]:
        tags.append("publique")
    if info["multicast"]:
        tags.append("multicast")
    if info["link_local"]:
        tags.append("link-local")
    if info["reserved"]:
        tags.append("réservée")
    if info["unspecified"]:
        tags.append("non-spécifiée")

    tags_str = ", ".join(tags) if tags else "sans catégorie particulière"
    return f"{ip_str} -> VALIDE ({info['version']}, {tags_str})"


def main() -> None:
    parser = argparse.ArgumentParser(description="Valide une ou plusieurs adresses IPv4/IPv6 et détecte leur type.")
    parser.add_argument("addresses", nargs="+", help="Adresse(s) IP à valider")
    args = parser.parse_args()

    exit_code = 0
    for addr in args.addresses:
        info = classify(addr)
        print(format_result(addr, info))
        if not info["valid"]:
            exit_code = 1

    sys.exit(exit_code)


if __name__ == "__main__":
    main()
