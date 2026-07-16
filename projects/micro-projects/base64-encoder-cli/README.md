# base64-encoder-cli

Encode ou décode du texte ou un fichier en base64. Supporte l'entrée directe en argument
(`-t`) ou depuis un fichier (`-f`), et la sortie vers un fichier (`-o`) ou stdout.

## Lancer

```bash
python base64_tool.py --encode -t "hello world"
python base64_tool.py --decode -t "aGVsbG8gd29ybGQ="
python base64_tool.py --encode -f photo.png -o photo.b64.txt
```

## Exemple d'usage

```bash
$ python base64_tool.py --encode -t "hello world"
aGVsbG8gd29ybGQ=

$ python base64_tool.py --decode -t "aGVsbG8gd29ybGQ="
hello world

$ python base64_tool.py --encode -f document.txt
```

Modes : `--encode` / `--decode` (exclusifs). Source : `-t/--text` ou `-f/--file` (exclusifs).
Pour un contenu binaire décodé, utilisez `-o` afin de l'écrire correctement en fichier.
