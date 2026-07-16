# json-to-csv-cli

Convertit un fichier JSON contenant une liste d'objets plats en CSV. Inverse de
`csv-to-json-cli`. Les colonnes sont déduites de l'union des clés rencontrées.

## Lancer

```bash
python json_to_csv.py data.json
python json_to_csv.py data.json -o data.csv
```

## Exemple d'usage

```bash
$ cat data.json
[{"name": "Alice", "age": 30}, {"name": "Bob", "age": 25}]

$ python json_to_csv.py data.json
name,age
Alice,30
Bob,25
```

Si un objet n'a pas toutes les colonnes, la cellule correspondante est laissée vide.
