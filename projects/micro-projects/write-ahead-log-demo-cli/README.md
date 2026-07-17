# write-ahead-log-demo-cli

Write-ahead log minimal : chaque operation est ecrite sur disque AVANT d'etre appliquee en memoire.
Au redemarrage, on rejoue le journal - le mecanisme derriere la durabilite de Postgres/Kafka.

## Lancer
```bash
javac WriteAheadLog.java && java WriteAheadLog
```
