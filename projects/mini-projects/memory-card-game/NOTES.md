# memory-card-game

Jeu de memory (8 paires d'emojis), compteur de coups, détection de victoire.

## Stack
Vanilla JS/HTML/CSS, zéro dépendance.

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `app.js` — `EMOJIS` (8 faces), `shuffle()` (Fisher-Yates), `flipCard(id)`
  gère la logique de comparaison à 2 cartes retournées, `lockBoard` bloque les
  clics pendant l'animation de non-match, `checkWin()`.
- `index.html` / `style.css` — grille 4x4 (16 cartes = 8 paires).

## Notes
- Aucune persistance : nouvelle partie à chaque `init()`/rechargement.
