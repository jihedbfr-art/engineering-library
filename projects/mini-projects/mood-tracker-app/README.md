# Mood Tracker App

## But
Enregistrement quotidien d'humeur (échelle emoji 1-5) avec note optionnelle,
historique visualisé en grille calendrier (mois courant, navigation prev/next).

## Mini-architecture
- `index.html` : formulaire du jour (sélecteur d'humeur + note) + calendrier.
- `style.css` : grille calendrier en CSS Grid (7 colonnes), cellule du jour
  courant mise en évidence.
- `app.js` : entrées persistées dans `localStorage` sous forme
  `{ "YYYY-MM-DD": { mood, note } }` (clé `mood-tracker-app:entries`),
  `renderCalendar()` reconstruit la grille du mois affiché (`viewYear`/`viewMonth`)
  avec l'emoji d'humeur par jour.

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique
Vanilla JS/HTML/CSS + `localStorage` comme imposé. Le calendrier est en pure
CSS Grid, pas de librairie de calendrier.
