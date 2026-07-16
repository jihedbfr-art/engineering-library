#!/usr/bin/env python3
"""Decode le header et le payload d'un JWT (JSON Web Token).

AVERTISSEMENT DE SECURITE : ce script NE VERIFIE PAS la signature du token.
Il se contente de decoder le base64url du header et du payload, exactement comme
n'importe qui peut le faire sur jwt.io. Un token affiche comme "decode" ici n'est
PAS un token valide/authentique : ne jamais faire confiance a son contenu sans
verifier la signature avec la cle/secret appropriee cote serveur.
"""
import argparse
import base64
import json
import sys

WARNING = (
    "AVERTISSEMENT: ce decodeur ne verifie PAS la signature du JWT. "
    "Le contenu affiche peut avoir ete falsifie. Ne jamais faire confiance "
    "a ces donnees sans validation de signature cote serveur."
)


def base64url_decode(segment):
    """Ajoute le padding manquant puis decode en base64url."""
    padding = "=" * (-len(segment) % 4)
    return base64.urlsafe_b64decode(segment + padding)


def decode_jwt(token):
    parts = token.split(".")
    if len(parts) != 3:
        raise ValueError(f"JWT invalide: attendu 3 segments (header.payload.signature), trouve {len(parts)}")

    header_raw, payload_raw, _signature_raw = parts

    header = json.loads(base64url_decode(header_raw))
    payload = json.loads(base64url_decode(payload_raw))

    return header, payload


def main():
    parser = argparse.ArgumentParser(
        description="Decode le header et le payload d'un JWT (SANS verifier la signature)."
    )
    parser.add_argument("token", help="Le JWT complet (header.payload.signature)")
    args = parser.parse_args()

    print(WARNING, file=sys.stderr)
    print()

    try:
        header, payload = decode_jwt(args.token.strip())
    except (ValueError, json.JSONDecodeError, Exception) as e:
        print(f"Erreur de decodage: {e}", file=sys.stderr)
        sys.exit(1)

    print("=== HEADER ===")
    print(json.dumps(header, indent=2, ensure_ascii=False))
    print("\n=== PAYLOAD ===")
    print(json.dumps(payload, indent=2, ensure_ascii=False))
    print(f"\n({WARNING})")


if __name__ == "__main__":
    main()
