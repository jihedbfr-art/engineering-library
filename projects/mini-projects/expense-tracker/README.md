# expense-tracker

## But
Suivi de dépenses : ajout (montant, catégorie, date), total, graphique en barres CSS par catégorie.

## Architecture
Vanilla JS/HTML/CSS + `localStorage`. `app.js` gère l'état des dépenses, le calcul du total, l'agrégation
par catégorie et le rendu des barres (divs redimensionnées en `height` proportionnelle — pas de lib
de graphique).

## Lancer
Ouvrir `index.html` dans un navigateur.
