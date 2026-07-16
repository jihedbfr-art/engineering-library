# sitemap-generator-cli

Générateur de sitemap.xml à partir d'une liste d'URLs en fichier texte.

- **Stack** : Python 3 stdlib (`xml.etree.ElementTree`). Aucune dépendance.
- **Lancer** : `python sitemap_generator.py urls.txt -o sitemap.xml`
- **Tester rapidement** : avec un fichier de 2 URLs, le script doit générer un `sitemap.xml` valide avec 2 balises `<url>`.
- **Fichier clé** : `sitemap_generator.py` (script unique, `build_sitemap`).
- **Points d'attention** : rejette les URLs sans préfixe `http://`/`https://` (exit 1). `lastmod` = date du jour. `changefreq`/`priority` optionnels via flags CLI, absents du XML si non fournis. Lignes vides et commentaires `#` ignorés dans le fichier d'entrée.
