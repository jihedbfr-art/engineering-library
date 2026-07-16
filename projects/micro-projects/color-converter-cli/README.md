# color-converter-cli

Convertit une couleur entre les formats hex (#RRGGBB), RGB et HSL, dans n'importe quel sens.
Affiche systématiquement les trois représentations.

## Lancer

```bash
python color_converter.py --hex "#FF5733"
python color_converter.py --rgb "255,87,51"
python color_converter.py --hsl "11,100%,60%"
```

## Exemple d'usage

```bash
$ python color_converter.py --hex "#FF5733"
HEX: #FF5733
RGB: 255, 87, 51
HSL: 11, 100%, 60%
```

Une seule option d'entrée à la fois (`--hex`, `--rgb` ou `--hsl`, mutuellement exclusives).
