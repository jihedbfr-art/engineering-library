# env-file-parser-cli

Parse un fichier `.env`, valide la syntaxe `KEY=VALUE`, signale les doublons de clés et
les lignes invalides. Peut exporter le résultat vers JSON.

## Lancer

```bash
python env_parser.py .env
python env_parser.py .env --export-json config.json
```

## Exemple d'usage

```bash
$ cat .env
DB_HOST=localhost
DB_PORT=5432
DB_HOST=127.0.0.1
INVALID LINE

$ python env_parser.py .env
2 clé(s) valide(s) trouvée(s).
  DB_HOST=127.0.0.1
  DB_PORT=5432

1 doublon(s) de clé détecté(s) :
  - DB_HOST (redéfini ligne 3)

1 ligne(s) invalide(s) :
  - ligne 4: syntaxe invalide -> 'INVALID LINE'
```

Les lignes vides et les commentaires (`#`) sont ignorés. Les guillemets simples/doubles
autour de la valeur sont retirés.
