# fibonacci-generator-cli

Génère les N premiers termes de la suite de Fibonacci, ou calcule directement le terme
d'indice n via un calcul itératif en O(n) (pas de récursion naïve exponentielle).

## Lancer

```bash
python fibonacci.py -n 10
python fibonacci.py --term 30
```

## Exemple d'usage

```bash
$ python fibonacci.py -n 10
0 1 1 2 3 5 8 13 21 34

$ python fibonacci.py --term 30
832040
```
