# sitemap-generator-cli

Génère un `sitemap.xml` valide (schéma sitemaps.org) à partir d'une liste d'URLs
fournie dans un fichier texte, une URL par ligne. Options pour ajouter `changefreq`
et `priority` à chaque entrée.

## Lancer

```bash
python sitemap_generator.py urls.txt -o sitemap.xml --changefreq weekly --priority 0.8
```

## Exemple d'usage

```bash
$ cat urls.txt
https://example.com/
https://example.com/about

$ python sitemap_generator.py urls.txt
Sitemap généré: sitemap.xml (2 URLs)

$ cat sitemap.xml
<?xml version='1.0' encoding='UTF-8'?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url><loc>https://example.com/</loc><lastmod>2026-07-16</lastmod></url>
  <url><loc>https://example.com/about</loc><lastmod>2026-07-16</lastmod></url>
</urlset>
```
