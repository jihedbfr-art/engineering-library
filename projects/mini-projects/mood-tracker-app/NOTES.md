# mood-tracker-app

Suivi quotidien d'humeur (emoji 1-5 + note), historique en grille calendrier.

## Stack
Vanilla JS/HTML/CSS, zéro dépendance. Persistance via `localStorage`.

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `app.js` — `MOODS` (échelle 1-5 avec emoji), `entries` (objet clé =
  `YYYY-MM-DD`), `saveToday()` écrit l'entrée du jour, `renderCalendar()`
  dessine le mois affiché (`viewYear`/`viewMonth`, navigation prev/next).
- `index.html` / `style.css` — formulaire du jour + grille calendrier 7 colonnes.

## Notes
- Persistance : `localStorage` clé `mood-tracker-app:entries`.
- Une seule entrée par jour (écrasée si on ré-enregistre le même jour).
