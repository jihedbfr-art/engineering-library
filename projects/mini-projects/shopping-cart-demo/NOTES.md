# shopping-cart-demo

Mini-app de panier d'achat factice (pas de paiement réel).

## Stack
Vanilla JS/HTML/CSS, zéro dépendance, zéro build. Persistance via `localStorage`.

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .` pour un serveur statique.

## Fichiers clés
- `app.js` — catalogue `PRODUCTS` (10 articles codés en dur) + logique panier
  (`addToCart`, `removeFromCart`, `clearCart`, rendu `renderCatalog`/`renderCart`).
- `index.html` — catalogue + panneau panier toggleable.
- `style.css` — grille responsive pour les cartes produit.

## Notes
- Panier stocké dans `localStorage` sous la clé `shopping-cart-demo:cart`
  (objet `{ productId: quantité }`).
- Aucune logique serveur, aucun vrai paiement.
