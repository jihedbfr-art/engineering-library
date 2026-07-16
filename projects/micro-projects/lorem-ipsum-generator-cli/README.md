# lorem-ipsum-generator-cli

Génère du faux texte latin (lorem ipsum) pour remplir des maquettes ou des jeux de test.
Mode paragraphes (par défaut) ou mode nombre de mots exact.

## Lancer

```bash
python lorem_gen.py
python lorem_gen.py -p 2
python lorem_gen.py -w 20
python lorem_gen.py -p 1 --sentences-per-paragraph 3 --seed 42
```

## Exemple d'usage

```bash
$ python lorem_gen.py -w 8
Lorem ipsum dolor consectetur sed magna veniam quis

$ python lorem_gen.py -p 1
Ullamco nisi aliquip commodo consequat duis aute irure. ...
```

`--seed` fixe la graine aléatoire pour obtenir toujours le même texte.
