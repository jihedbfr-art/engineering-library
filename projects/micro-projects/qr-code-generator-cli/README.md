# qr-code-generator-cli

Génère un QR code à partir d'un texte ou d'une URL. Utilise le package `qrcode` s'il est
disponible ; sinon affiche un placeholder ASCII et explique comment installer la dépendance.

## Lancer

```bash
# Avec le package qrcode installé (pip install qrcode)
python qr_generator.py "https://example.com"

# Sauvegarder en image (nécessite qrcode + pillow: pip install qrcode[pil])
python qr_generator.py "https://example.com" -o qr.png
```

## Exemple d'usage

```bash
$ python qr_generator.py "hello world"
# Affiche le QR en ASCII dans le terminal si `qrcode` est installé

$ python qr_generator.py "hello world"
# Sans le package installé :
Le package 'qrcode' n'est pas installé. Installez-le avec :
    pip install qrcode
Affichage d'un placeholder ASCII (PAS un vrai QR code) en attendant :

+----------------------------------------+
|      QR PLACEHOLDER (non scannable)     |
+----------------------------------------+
| hello world                             |
+----------------------------------------+
```

## Compromis assumé

Sans le package `qrcode` (nécessite `pip install qrcode`, donc un accès réseau une fois),
le script reste utilisable hors-ligne grâce à un **placeholder ASCII-art** : ce n'est PAS un
vrai QR code scannable, juste un encadré visuel autour du texte pour indiquer que la génération
réelle est indisponible. Installez `qrcode` pour un vrai QR code fonctionnel.
