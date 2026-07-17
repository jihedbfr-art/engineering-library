# structured-log-parser-cli

Parse un fichier de logs (format `timestamp LEVEL message`) et compte les occurrences par niveau
(INFO/WARN/ERROR) - le genre de premier filtre avant Grafana/Loki.

## Lancer
```bash
javac LogParser.java && java LogParser app.log
```
