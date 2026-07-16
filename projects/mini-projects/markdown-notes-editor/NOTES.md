# markdown-notes-editor

Éditeur Markdown avec preview HTML temps réel et sauvegarde localStorage.

## Stack
Vanilla JS/HTML/CSS, zéro dépendance. Parseur Markdown maison (pas de librairie
externe type `marked`).

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `markdown.js` — `renderMarkdown(source)` : convertisseur Markdown → HTML
  (titres, gras, italique, listes, liens). `escapeHtml` échappe avant tout
  traitement inline pour éviter l'injection de balises.
- `app.js` — branche le `textarea` au preview (`render()` à chaque `input`),
  sauvegarde débouncée dans `localStorage` (clé `markdown-notes-editor:content`).
- `index.html` — layout deux colonnes.

## Notes
- Sous-ensemble Markdown supporté : `# à ######`, `**gras**`/`__gras__`,
  `*italique*`/`_italique_`, listes `- item`, liens `[texte](url)`. Pas de
  tables, citations, blocs de code, images.
