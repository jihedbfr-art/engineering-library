# markdown-to-html-cli

Convertisseur Markdown → HTML maison (sous-ensemble limité), sans lib externe.

- **Stack** : Python 3 stdlib (`re`, `argparse`). Aucune dépendance (pas de `markdown` pip).
- **Lancer** : `python md_to_html.py fichier.md` ou `cat fichier.md | python md_to_html.py`
- **Tester rapidement** : `printf '# Titre\n\n**gras**' | python md_to_html.py`
- **Fichier clé** : `md_to_html.py` (fonctions `inline_format` pour gras/italique/liens, `convert` pour titres/listes/paragraphes).
- **Points d'attention** : échappement HTML fait AVANT le formatage inline pour éviter l'injection. Pas de support imbriqué/tableaux/code.
