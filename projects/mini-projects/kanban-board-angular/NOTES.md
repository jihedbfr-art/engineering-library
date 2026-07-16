# kanban-board-angular

## Vue d'ensemble
Tableau kanban 3 colonnes avec drag & drop HTML5 natif et persistance `localStorage`. Vanilla JS,
pas Angular réel (voir README).

## Stack
HTML + CSS + JavaScript vanilla, zéro dépendance, zéro build.

## Commandes
Ouvrir `index.html` dans un navigateur.

## Fichiers clés
- `app.js` — état des tâches (id, texte, statut), logique drag & drop, persistance `localStorage`.
- `index.html` — 3 colonnes (`data-status="todo|doing|done"`) + formulaire d'ajout.
- `style.css` — style des colonnes et cartes.
