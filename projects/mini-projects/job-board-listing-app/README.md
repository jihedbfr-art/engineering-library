# Job Board Listing App

## But
Liste d'offres d'emploi codées en dur (15 offres : titre/entreprise/lieu/type),
filtres par lieu et type de contrat, recherche par mot-clé (titre, entreprise,
description).

## Mini-architecture
- `index.html` : barre de filtres (recherche + 2 selects) + liste de cartes.
- `style.css` : cartes offre, tags lieu/type.
- `jobs-data.js` : tableau `JOBS` (15 offres codées en dur).
- `app.js` : `populateFilters()` génère dynamiquement les options des selects
  à partir des valeurs présentes dans `JOBS` ; `render()` filtre par recherche
  + lieu + type à chaque changement.

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique
Vanilla JS/HTML/CSS comme imposé, zéro dépendance, zéro backend. Toutes les
offres sont statiques dans `jobs-data.js`.
