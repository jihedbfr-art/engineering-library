# snake-game-js

Snake jouable au clavier avec canvas HTML5, score, game over, restart.

## Stack
Vanilla JS pur + `<canvas>`, zéro dépendance.

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `app.js` — `init()` réinitialise l'état, `tick()` (boucle `setInterval`,
  120 ms) déplace le serpent, détecte collisions (`hitsWall`/`hitsSelf`),
  `draw()` redessine le canvas à chaque tick, gestion clavier (flèches +
  Espace pour redémarrer).
- `index.html` / `style.css` — canvas 400x400 (grille logique 20x20 cellules
  de 20px), overlay game over.

## Notes
- `direction`/`nextDirection` séparés pour empêcher un demi-tour instantané
  (appui opposé à la direction courante ignoré).
- Aucune persistance de high score.
