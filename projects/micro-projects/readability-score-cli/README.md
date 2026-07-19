# readability-score-cli

Calcule le score **Flesch Reading Ease** et le **Flesch-Kincaid Grade Level** d'un texte anglais.

## Lancer

```bash
python readability.py my-article.txt
cat my-article.txt | python readability.py -
```

## Exemple

```bash
$ echo "The cat sat on the mat. It was a sunny day." | python readability.py -
Phrases         : 2
Mots            : 10
Syllabes (est.) : 12
Flesch Reading Ease : 100.7 (tres facile (CE1-CE2))
Flesch-Kincaid Grade Level : -0.1
```

## Limites

Le comptage de syllabes est une heuristique (groupes de voyelles consécutifs), pas un vrai
découpage phonétique — il se trompe sur les mots avec des diphtongues ou des `e` muets
irréguliers. Assez fiable pour comparer deux versions du même texte entre elles ; pas fait
pour être cité comme score officiel dans un rapport.
