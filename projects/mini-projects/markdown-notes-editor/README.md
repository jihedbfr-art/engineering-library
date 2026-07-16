# Markdown Notes Editor

## But
Éditeur avec zone de texte Markdown à gauche et preview HTML en temps réel à
droite, sauvegarde automatique dans `localStorage`.

## Mini-architecture
- `index.html` : deux colonnes (`textarea` + `div.preview`).
- `markdown.js` : convertisseur Markdown → HTML maison (`renderMarkdown`),
  sous-ensemble volontairement limité : titres `#`..`######`, gras `**`/`__`,
  italique `*`/`_`, listes `-`/`*`, liens `[texte](url)`. Échappement HTML avant
  application des styles inline (protection XSS basique).
- `app.js` : branchement `input` → `render()` + sauvegarde `localStorage`
  débouncée (400 ms) sous la clé `markdown-notes-editor:content`.

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique
Pas de librairie Markdown externe (type `marked`/`showdown`) : parseur maison
minimal dans `markdown.js`, pour rester zéro dépendance comme imposé. Le
sous-ensemble supporté est volontairement réduit (pas de tables, citations,
blocs de code) pour tenir dans le budget du mini-projet.
