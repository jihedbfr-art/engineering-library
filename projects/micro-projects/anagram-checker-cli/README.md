# anagram-checker-cli

Vérifie si deux mots ou phrases sont des anagrammes (ignore espaces, casse et
ponctuation), et propose un mode `find` qui regroupe les anagrammes présents dans une
liste de mots fournie en fichier.

## Lancer

```bash
python anagram_checker.py check "Listen" "Silent"
python anagram_checker.py find words.txt
```

## Exemple d'usage

```bash
$ python anagram_checker.py check "Listen" "Silent"
'Listen' et 'Silent' sont des anagrammes.

$ python anagram_checker.py find words.txt
listen, silent, enlist
```
