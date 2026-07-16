# tic-tac-toe-angular

Morpion 2 joueurs local, détection victoire/égalité, reset.

## Stack
Vanilla JS/HTML/CSS — **PAS de vraie CLI Angular** (voir README.md).

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `app.js` — état `board` (tableau de 9), `WIN_LINES`, `checkWinner()`,
  `playMove(idx)` alterne `currentPlayer`, `reset()`.
- `index.html` / `style.css` — grille 3x3 en CSS Grid.

## Notes
- Aucune persistance : le score n'est pas cumulé entre parties (juste reset).
