# inventory-tracker

CRUD d'articles de stock avec alerte visuelle sur seuil bas, recherche et filtre.

## Stack
Vanilla JS/HTML/CSS, zéro dépendance. Persistance via `localStorage`.

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `app.js` — état `items` (`{id, name, qty, threshold}`), `addItem`/`updateQty`/
  `deleteItem`, `render()` applique recherche + filtre "stock bas" avant d'afficher.
- `index.html` — formulaire d'ajout, barre recherche/filtre, tableau.
- `style.css` — ligne `.low-stock` surlignée quand `qty < threshold`.

## Notes
- Persistance : `localStorage` clé `inventory-tracker:items`.
- Le seuil d'alerte est par article, saisi à la création (défaut 5).
