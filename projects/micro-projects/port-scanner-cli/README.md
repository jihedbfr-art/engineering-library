# port-scanner-cli

Scanner de ports TCP **éducatif**, limité à `localhost`, qui teste l'ouverture de ports sur
une plage raisonnable (max 1024 ports par scan).

> **AVERTISSEMENT SÉCURITÉ** : à utiliser **uniquement sur des machines dont vous êtes
> propriétaire ou que vous êtes explicitement autorisé à tester**. Scanner des ports sur des
> systèmes tiers sans autorisation peut être illégal. Ce script est volontairement bridé :
> - cible par défaut et **uniquement autorisée** = `127.0.0.1` / `localhost` / `::1`
> - **pas** de scan de plages IP entières
> - **pas** de mode scan agressif/masse (plage limitée à 1024 ports maximum)

## Lancer

```bash
python port_scanner.py --start-port 1 --end-port 1024
python port_scanner.py --start-port 20 --end-port 100 --timeout 0.3
```

## Exemple

```bash
$ python port_scanner.py --start-port 1 --end-port 20
AVERTISSEMENT: ce scanner de ports est a usage EDUCATIF uniquement.
...
Scan de 127.0.0.1, ports 1-20 (timeout 0.5s/port)...

Scan termine. 0 port(s) ouvert(s) sur 20 scanne(s).
```
