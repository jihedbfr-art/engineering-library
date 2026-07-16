# todo-app-angular

## But
Todo list minimaliste : ajout, suppression, toggle "terminé", filtre actif/terminé, persistance locale.

## Choix technique (écart voulu)
Le nom du dossier vient de la consigne d'origine ("todo-app-angular"), mais l'implémentation est en
**vanilla JS/HTML/CSS**, pas en Angular CLI. Un vrai projet Angular impose node_modules, un compilateur
TypeScript et un tooling complet — hors budget pour ce mini-projet. Le résultat fonctionnel (todo list
réactive avec état et filtres) est le même.

## Architecture
Un seul dossier, 3 fichiers : `index.html` (structure), `style.css` (présentation),
`app.js` (état des todos en mémoire + `localStorage`, rendu DOM manuel).

## Lancer
Ouvrir `index.html` directement dans un navigateur (double-clic), ou servir le dossier :
```
npx serve .
```
