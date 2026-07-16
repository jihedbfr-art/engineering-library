# env-file-parser-cli

Parseur/validateur de fichiers `.env` avec export JSON optionnel.

- **Stack** : Python 3 stdlib (`re`, `json`, `argparse`). Aucune dépendance.
- **Lancer** : `python env_parser.py .env --export-json config.json`
- **Tester rapidement** : un `.env` avec `A=1` puis `A=2` doit signaler un doublon sur `A`.
- **Fichier clé** : `env_parser.py`, fonction `parse_env` (regex `KEY_VALUE_RE`).
- **Points d'attention** : dernière valeur d'une clé dupliquée gagne dans l'export, mais le
  doublon est quand même signalé. Lignes vides/commentaires (`#`) ignorés silencieusement.
