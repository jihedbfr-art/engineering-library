# hash-generator-cli

Calcule le hash MD5, SHA1, SHA256 ou SHA512 d'un fichier ou d'une chaîne de caractères
directement en ligne de commande, sans dépendance externe.

## Lancer

```bash
python hash_generator.py --text "hello world" --algo sha256
python hash_generator.py --file chemin/vers/fichier.txt --algo md5
```

## Exemple

```bash
$ python hash_generator.py --text "hello" --algo sha256
sha256: 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
```
