# color-converter-cli

Convertisseur de couleurs hex / RGB / HSL bidirectionnel.

- **Stack** : Python 3 stdlib (`colorsys`, `re`, `argparse`). Aucune dépendance.
- **Lancer** : `python color_converter.py --hex "#FF5733"`
- **Tester rapidement** : `python color_converter.py --rgb "255,0,0"` doit donner `HEX: #FF0000`.
- **Fichier clé** : `color_converter.py` (script unique).
- **Points d'attention** : `colorsys` utilise l'ordre HLS (pas HSL) — conversion inversée dans
  `rgb_to_hsl`/`hsl_to_rgb`. Une seule des trois options d'entrée acceptée à la fois.
