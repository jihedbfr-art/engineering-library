# Memory Card Game

## But
Jeu de memory : 8 paires de cartes (emojis), compteur de coups, détection de
victoire quand toutes les paires sont trouvées.

## Mini-architecture
- `index.html` : grille de cartes + stats + message de victoire.
- `style.css` : grille 4x4 en CSS Grid, cartes retournées/appariées stylées.
- `app.js` : deck mélangé (`shuffle`, Fisher-Yates) à partir de 8 emojis
  dupliqués, état par carte (`isFlipped`, `isMatched`), `flipCard()` compare
  les 2 dernières cartes retournées, `lockBoard` empêche de cliquer pendant
  l'animation de non-match (700 ms).

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique
Vanilla JS/HTML/CSS comme imposé, zéro dépendance. Les emojis servent de faces
de carte (pas d'images externes à charger).
