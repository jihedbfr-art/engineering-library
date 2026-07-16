# Shopping Cart Demo

## But
Petite boutique en ligne factice : catalogue de 10 produits, ajout/retrait au panier,
calcul du total. **Aucun paiement réel** — c'est une démo front-end pure, il n'y a pas
de commande, pas de transaction, pas de backend.

## Mini-architecture
- `index.html` : structure (catalogue + panneau panier).
- `style.css` : mise en page en grille responsive.
- `app.js` : catalogue codé en dur (tableau `PRODUCTS`), état du panier `{ id: qty }`
  persisté dans `localStorage` (clé `shopping-cart-demo:cart`), rendu re-déclenché à
  chaque modification.

## Lancer
Ouvrir `index.html` directement dans un navigateur (double-clic), ou servir le
dossier avec un serveur statique, ex. :
```
npx serve .
```

## Choix technique
Vanilla JS/HTML/CSS comme imposé — pas de framework, pas de build step, zéro
dépendance. Le panier survit au rechargement de page grâce à `localStorage`.
