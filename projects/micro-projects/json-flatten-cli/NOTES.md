# json-flatten-cli

Aplatisseur JSON, notation dot + index de tableau.

## Stack

Python 3, stdlib uniquement (`json`, `argparse`).

## Lancer / tester

```bash
echo '{"a":1,"b":{"c":2}}' | python flatten.py -
```

## Fichiers clés

- `flatten.py` — `flatten()` recursif : dict -> `prefix.key`, list -> `prefix[index]`,
  scalaire -> valeur terminale. Dict/liste vide preserve tel quel (pas de cle perdue).

## Points d'attention

- Pas de gestion des cles contenant deja un point (`{"a.b": 1}`) — le resultat aplati sera
  ambigu avec une vraie imbrication `{"a":{"b":1}}`. Non gere volontairement, cas rare en
  pratique et corriger proprement demanderait un separateur configurable.
