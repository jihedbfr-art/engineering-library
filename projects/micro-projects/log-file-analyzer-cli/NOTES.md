# log-file-analyzer-cli

Parse un access log Apache/Nginx (Combined Log Format) et affiche stats codes HTTP / top IPs / top endpoints.

## Stack

Python 3, stdlib uniquement (`re`, `collections.Counter`, `argparse`).

## Lancer / tester

```bash
python log_analyzer.py access.log --top 10
```

## Fichiers clés

- `log_analyzer.py` — `LOG_PATTERN` (regex Combined Log Format), `parse_log_file()` extrait
  les entrées ligne par ligne, `analyze()` calcule les `Counter`, `print_report()` affiche.

## Points d'attention

- Les lignes qui ne matchent pas le regex sont comptées et signalées en fin de rapport,
  jamais silencieusement ignorées.
