# word-counter-cli

Compte les mots, lignes et caractères d'un fichier texte (ou stdin) et affiche
le top 10 des mots les plus fréquents.

## Lancer

```bash
python word_counter.py texte.txt
# ou via stdin
cat texte.txt | python word_counter.py
```

## Exemple d'usage

```bash
$ printf 'le chat mange le poisson\nle chat dort\n' | python word_counter.py
Lignes     : 2
Mots       : 8
Caractères : 38

Top 10 des mots les plus fréquents :
  le                   3
  chat                 2
  mange                1
  poisson              1
  dort                 1
```
