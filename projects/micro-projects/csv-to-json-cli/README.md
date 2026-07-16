# csv-to-json-cli

Convertit un fichier CSV (ou stdin) en JSON — une liste d'objets, une clé par colonne
d'en-tête. Gère automatiquement les en-têtes via `csv.DictReader`.

## Lancer

```bash
python csv_to_json.py data.csv
# ou via stdin
cat data.csv | python csv_to_json.py
```

## Exemple d'usage

```bash
$ printf 'name,age\nAlice,30\nBob,25\n' | python csv_to_json.py
[
  {
    "name": "Alice",
    "age": "30"
  },
  {
    "name": "Bob",
    "age": "25"
  }
]

$ python csv_to_json.py data.csv -d ";" -o data.json
```

Options : `-d/--delimiter` (défaut `,`), `-o/--output`, `--indent`.
