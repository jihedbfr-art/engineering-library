# jwt-decoder-cli

Décode header + payload d'un JWT (base64url/JSON) SANS vérifier la signature.

## Stack

Python 3, stdlib uniquement (`base64`, `json`, `argparse`).

## Lancer / tester

```bash
python jwt_decoder.py "<jwt>"
```

## Fichiers clés

- `jwt_decoder.py` — `base64url_decode()` gère le padding manquant, `decode_jwt()` split
  et décode les 2 premiers segments (header, payload) ; la signature n'est jamais vérifiée.

## Points d'attention

- AVERTISSEMENT affiché en stderr ET en fin de sortie stdout à chaque exécution : aucune
  vérification de signature n'est faite. Ne jamais utiliser ce script pour valider un token.
