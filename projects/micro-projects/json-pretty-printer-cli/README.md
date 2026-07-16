# json-pretty-printer-cli

Réindente proprement un fichier JSON (ou l'entrée standard), avec option de tri des clés.
Utile pour lire des payloads JSON minifiés ou mal formatés.

## Lancer

```bash
python json_pretty.py data.json
# ou via stdin
cat data.json | python json_pretty.py
```

## Exemple d'usage

```bash
$ echo '{"b":2,"a":1}' | python json_pretty.py --sort-keys
{
  "a": 1,
  "b": 2
}

$ python json_pretty.py data.json -o data.formatted.json
```

Options : `--sort-keys`, `--indent N` (défaut 2), `-o/--output FICHIER`.
