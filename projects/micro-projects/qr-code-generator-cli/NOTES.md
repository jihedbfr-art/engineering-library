# qr-code-generator-cli

Générateur de QR code avec fallback ASCII hors-ligne si le package `qrcode` est absent.

- **Stack** : Python 3. Dépendance optionnelle `qrcode` (import protégé par try/except) ; aucune dépendance obligatoire.
- **Lancer** : `python qr_generator.py "texte"` (ajouter `-o qr.png` pour sauvegarder, nécessite `qrcode[pil]`).
- **Tester rapidement** : `python qr_generator.py "test"` doit fonctionner avec ou sans `qrcode` installé.
- **Fichier clé** : `qr_generator.py` (`generate_real_qr` si dispo, sinon `generate_ascii_placeholder`).
- **Points d'attention** : sans `qrcode` installé, le fallback ASCII n'est PAS un vrai QR scannable — juste un repère visuel. Documenté dans le README.
