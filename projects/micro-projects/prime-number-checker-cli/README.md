# prime-number-checker-cli

Vérifie si un nombre est premier (test par division jusqu'à sa racine carrée), et
propose une option `--list` pour lister tous les nombres premiers jusqu'à N via le
crible d'Ératosthène.

## Lancer

```bash
python prime_checker.py 97
python prime_checker.py 50 --list
```

## Exemple d'usage

```bash
$ python prime_checker.py 97
97 est premier.

$ python prime_checker.py 50 --list
2 3 5 7 11 13 17 19 23 29 31 37 41 43 47
```
