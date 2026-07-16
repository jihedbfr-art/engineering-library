# Inventory Tracker

## But
CRUD d'articles de stock (nom, quantité, seuil d'alerte), alerte visuelle si la
quantité passe sous le seuil, recherche par nom, filtre "stock bas uniquement".

## Mini-architecture
- `index.html` : formulaire d'ajout + barre de recherche/filtre + tableau.
- `style.css` : mise en forme du tableau, ligne surlignée en jaune si stock bas.
- `app.js` : tableau `items` en mémoire, persisté dans `localStorage`
  (clé `inventory-tracker:items`), rendu filtré à chaque changement (`render()`).

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique
Vanilla JS/HTML/CSS + `localStorage` comme imposé — pas de backend, pas de base de
données. Le seuil d'alerte est défini par article (champ du formulaire, défaut 5).
