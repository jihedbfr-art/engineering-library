# weather-dashboard-angular

## But
Dashboard météo sur 7 jours pour plusieurs villes, avec cartes température/icônes.

## Choix technique (écart voulu)
Le nom du dossier vient de la consigne d'origine ("weather-dashboard-angular"), mais l'implémentation
est en **vanilla JS/HTML/CSS**, pas Angular (même raison que `todo-app-angular` : pas de tooling CLI
dans ce budget). **Aucun appel API météo réel** : les données (`data.js`) sont 100% simulées et codées
en dur pour 4 villes (Paris, Tunis, Londres, Tokyo) sur 7 jours — pas de garantie d'accès internet ni
de clé API dans cet environnement. C'est explicitement affiché dans l'UI ("⚠️ Données 100% simulées").

## Architecture
- `data.js` : jeu de données statique (villes -> 7 jours -> icône emoji, températures, description).
- `app.js` : sélecteur de ville + rendu des cartes du dashboard.
- `index.html` / `style.css` : structure et présentation.

## Lancer
Ouvrir `index.html` dans un navigateur.
