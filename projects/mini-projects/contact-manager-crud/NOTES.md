# contact-manager-crud

CRUD de contacts (nom, email, téléphone, notes) avec recherche par nom.

## Stack
Vanilla JS/HTML/CSS, zéro dépendance. Persistance via `localStorage`.

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `app.js` — état `contacts`, formulaire unique pour créer/éditer (`editIdInput`
  détermine le mode), `startEdit`/`resetForm`/`deleteContact`, `render()` filtre
  par nom et trie alphabétiquement.
- `index.html` — formulaire + liste de cartes contact.
- `style.css` — mise en forme des cartes.

## Notes
- Persistance : `localStorage` clé `contact-manager-crud:contacts`.
