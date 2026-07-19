# url-query-parser-cli

Decompose une URL et parse sa query string, en gerant les cles repetees.

## Stack

Python 3, stdlib uniquement (`urllib.parse`, `argparse`, `json`).

## Lancer / tester

```bash
python parse_query.py "https://example.com/page?a=1&a=2"
```

## Fichiers clés

- `parse_query.py` — `urlparse()` pour decomposer, `parse_qs(keep_blank_values=True)` pour
  la query string (garde les cles avec valeur vide plutot que de les dropper silencieusement,
  comportement par defaut de `parse_qs` qu'il vaut mieux desactiver explicitement).

## Points d'attention

- Rejette une URL sans schema/hote (`erreur, code 1`) plutot que de deviner — un chemin
  relatif seul (`/page?a=1`) n'est pas une URL absolue au sens de cet outil.
