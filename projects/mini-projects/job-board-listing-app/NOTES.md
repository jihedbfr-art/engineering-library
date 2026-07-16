# job-board-listing-app

Liste d'offres d'emploi (15, codées en dur) avec filtres lieu/type et recherche.

## Stack
Vanilla JS/HTML/CSS, zéro dépendance, zéro backend.

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `jobs-data.js` — tableau `JOBS` (15 offres : id, title, company, location,
  type, desc).
- `app.js` — `populateFilters()` construit les `<select>` lieu/type à partir
  des valeurs uniques de `JOBS`, `render()` applique recherche texte + filtres.
- `index.html` / `style.css` — barre de filtres + cartes offre.

## Notes
- Aucune persistance, aucune pagination (15 offres tiennent sur une page).
