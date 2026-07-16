# kanban-board-angular

## But
Tableau kanban 3 colonnes (À faire / En cours / Terminé) avec glisser-déposer natif pour changer une
tâche de colonne, et persistance locale.

## Choix technique (écart voulu)
Vanilla JS/HTML/CSS, pas Angular (même raison budget/tooling que les autres mini-projets "-angular").
Drag & drop via l'API HTML5 native (`draggable`, `dragstart`/`dragover`/`drop`) — aucune librairie.

## Architecture
Un seul dossier : `index.html` (3 colonnes + formulaire d'ajout), `app.js` (état des tâches en
mémoire + `localStorage`, logique drag & drop), `style.css`.

## Lancer
Ouvrir `index.html` dans un navigateur.
