# url-query-parser-cli

Décompose une URL (schéma, hôte, port, chemin, fragment) et parse sa query string en JSON,
en gérant correctement les clés répétées (`?tag=a&tag=b` → `["a", "b"]`).

## Lancer

```bash
python parse_query.py "https://example.com/path?utm_source=newsletter&tag=a&tag=b"
python parse_query.py "https://example.com/path?a=1&b=2" --flatten
```

## Exemple

```bash
$ python parse_query.py "https://example.com:8443/page?utm_source=x&tag=a&tag=b#top"
{
  "scheme": "https",
  "host": "example.com",
  "port": 8443,
  "path": "/page",
  "fragment": "top",
  "query": {
    "utm_source": ["x"],
    "tag": ["a", "b"]
  }
}
```

`--flatten` renvoie une valeur scalaire au lieu d'une liste à un élément quand une clé
n'apparaît qu'une fois — plus lisible pour un debug rapide, mais perd l'information qu'une
autre requête avec la même clé répétée aurait donné une liste. À utiliser pour lire, pas
pour comparer deux URLs programmatiquement.
