# habit-tracker

## But
Liste d'habitudes avec check-in quotidien (grille "heatmap" des 30 derniers jours) et compteur de série
(streak).

## Architecture
Vanilla JS/HTML/CSS + `localStorage`. Chaque habitude a un tableau `checkins` de dates (`YYYY-MM-DD`)
cochées. La heatmap est une grille CSS (`display: grid`) de 30 cellules cliquables ; le streak compte
les jours consécutifs cochés en partant d'aujourd'hui vers le passé.

## Lancer
Ouvrir `index.html` dans un navigateur.
