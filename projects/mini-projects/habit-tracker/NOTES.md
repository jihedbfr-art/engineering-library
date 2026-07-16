# habit-tracker

## Vue d'ensemble
Suivi d'habitudes avec heatmap CSS grid des 30 derniers jours et streak counter, persistance
`localStorage`.

## Stack
HTML + CSS + JavaScript vanilla, zéro dépendance, zéro build.

## Commandes
Ouvrir `index.html` dans un navigateur.

## Fichiers clés
- `app.js` — état des habitudes (nom, checkins[] de dates), calcul du streak, rendu de la heatmap.
- `index.html` — formulaire d'ajout + liste des habitudes.
- `style.css` — grille CSS de la heatmap (`.heatmap { display: grid; grid-template-columns: repeat(10, 1fr) }`).
