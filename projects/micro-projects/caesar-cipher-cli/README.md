# caesar-cipher-cli

Chiffre ou déchiffre un texte avec le chiffre de César (décalage alphabétique). Inclut un
mode brute-force qui affiche les 26 décalages possibles pour casser un message sans clé connue.

## Lancer

```bash
python caesar.py "Hello World" -s 3
python caesar.py "Khoor Zruog" -s -3
python caesar.py "Khoor Zruog" -b
```

## Exemple d'usage

```bash
$ python caesar.py "Hello World" -s 3
Khoor Zruog

$ python caesar.py "Khoor Zruog" -b
 0: Khoor Zruog
 1: Lippr Asvph
 ...
 23: Hello World
 ...
```

Un décalage négatif déchiffre. La casse est préservée, la ponctuation n'est pas modifiée.
