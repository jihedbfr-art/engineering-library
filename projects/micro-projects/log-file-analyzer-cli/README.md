# log-file-analyzer-cli

Parse un fichier de log au format Apache/Nginx access log courant (Combined Log Format) et
affiche des statistiques : nombre de requêtes par code HTTP, top IPs, top endpoints.

## Lancer

```bash
python log_analyzer.py chemin/vers/access.log --top 10
```

## Exemple

```bash
$ python log_analyzer.py sample.log --top 5
Total de requetes analysees : 4

=== Requetes par code HTTP ===
  200: 2
  401: 1
  404: 1

=== Top 5 IPs ===
  127.0.0.1: 3 requete(s)
  192.168.1.5: 1 requete(s)
```
