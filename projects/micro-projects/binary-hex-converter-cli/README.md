# binary-hex-converter-cli

Convertit un nombre entre binaire, hexadécimal, décimal et octal. Détecte la base source
automatiquement via le préfixe (`0x`, `0b`, `0o`), sinon suppose du décimal.

## Lancer

```bash
python base_converter.py 255
python base_converter.py 0xFF -t dec
python base_converter.py 10 -f dec -t bin
```

## Exemple d'usage

```bash
$ python base_converter.py 255
decimal: 255
binary:  11111111
hex:     ff
octal:   377

$ python base_converter.py 0xFF -t dec
255

$ python base_converter.py 10 -f dec -t bin
1010
```
