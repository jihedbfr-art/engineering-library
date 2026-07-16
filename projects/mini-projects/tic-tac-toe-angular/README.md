# Tic Tac Toe

## But
Morpion 2 joueurs sur la même machine (X/O en alternance), détection de victoire
et d'égalité, mise en évidence de la ligne gagnante, bouton reset.

## Mini-architecture
- `index.html` : grille + statut + bouton reset.
- `style.css` : grille 3x3 en CSS Grid.
- `app.js` : tableau `board` de 9 cases, `WIN_LINES` (8 combinaisons gagnantes),
  `checkWinner()` teste chaque ligne + cas d'égalité (plateau plein).

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique — écart par rapport au nom du dossier
Le dossier s'appelle `tic-tac-toe-angular` mais un vrai projet Angular CLI est
disproportionné pour un morpion à une seule vue et un budget de ~10 min.
**Implémentation en Vanilla JS/HTML/CSS**, zéro dépendance, zéro build.
