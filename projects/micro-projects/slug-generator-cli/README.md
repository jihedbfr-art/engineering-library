# slug-generator-cli

Convertit un texte libre en slug URL-friendly : minuscules, tirets, accents et caractères
spéciaux supprimés.

## Lancer

```bash
python slug_generator.py "Café à Côté : Éléphant & Cie !!"
```

## Exemple

```bash
$ python slug_generator.py "Café à Côté : Éléphant & Cie !!"
cafe-a-cote-elephant-cie

$ python slug_generator.py "  Hello   World  "
hello-world
```
