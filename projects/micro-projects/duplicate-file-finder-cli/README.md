# duplicate-file-finder-cli

Scanne un dossier récursivement, calcule le hash SHA-256 de chaque fichier et regroupe/affiche
les fichiers ayant un contenu identique (doublons), avec l'espace disque gaspillé estimé.

## Lancer

```bash
python duplicate_finder.py <dossier>
```

## Exemple

```bash
python duplicate_finder.py C:\Users\jihed\Documents

Groupe de doublons (sha256=3f2a9c1b4e5d..., 2 fichiers, 15420 octets chacun):
  - C:\Users\jihed\Documents\rapport.pdf
  - C:\Users\jihed\Documents\backup\rapport.pdf

1 groupe(s) de doublons trouve(s). Espace gaspille: 15420 octets.
```
