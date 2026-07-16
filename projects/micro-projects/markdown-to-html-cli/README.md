# markdown-to-html-cli

Convertit un sous-ensemble de Markdown (titres, gras, italique, listes, liens, paragraphes) en HTML,
sans aucune dépendance externe — implémentation maison en stdlib Python.

## Lancer

```bash
python md_to_html.py notes.md
# ou via stdin
cat notes.md | python md_to_html.py
```

## Exemple d'usage

```bash
$ printf '# Titre\n\nUn paragraphe avec **gras** et *italique*.\n\n- item un\n- item deux\n\n[Lien](https://example.com)\n' | python md_to_html.py
<h1>Titre</h1>
<p>Un paragraphe avec <strong>gras</strong> et <em>italique</em>.</p>
<ul>
<li>item un</li>
<li>item deux</li>
</ul>
<p><a href="https://example.com">Lien</a></p>
```

Supporté : `#` à `######`, `**gras**`, `*italique*`, `- liste`, `[texte](url)`, paragraphes.
Non supporté : tableaux, code blocks, citations, listes numérotées, listes imbriquées.
