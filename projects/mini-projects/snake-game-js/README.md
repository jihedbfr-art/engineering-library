# Snake Game JS

## But
Snake jouable au clavier (flèches), score, détection de collision (mur/soi-même),
game over avec overlay et redémarrage.

## Mini-architecture
- `index.html` : `<canvas>` 400x400 + score + overlay game over.
- `style.css` : thème sombre, overlay superposé au canvas.
- `app.js` : grille logique 20x20 (cellules de 20px), boucle de jeu via
  `setInterval` (120 ms/tick), état `snake` (liste de segments), `direction`/
  `nextDirection` pour éviter les demi-tours instantanés.

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique
Vanilla JS pur avec `<canvas>` comme imposé, zéro dépendance, zéro librairie de
jeu. Espace pour rejouer après game over, ou clic sur le bouton "Rejouer".
