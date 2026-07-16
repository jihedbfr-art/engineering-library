# morse-code-cli

Encode et décode du texte en code Morse. Les mots sont séparés par ` / ` en Morse,
les lettres par un espace.

## Lancer

```bash
python morse_tool.py -e "SOS HELLO"
python morse_tool.py -d "... --- ... / .... . .-.. .-.. ---"
```

## Exemple d'usage

```bash
$ python morse_tool.py -e "SOS"
... --- ...

$ python morse_tool.py -d "... --- ..."
SOS
```

Table de correspondance codée en dur (lettres, chiffres, ponctuation courante).
