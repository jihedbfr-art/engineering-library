# quiz-app-angular

Quiz à choix multiples, 10 questions codées en dur, score final.

## Stack
Vanilla JS/HTML/CSS — **PAS de vraie CLI Angular** (nom de dossier historique, voir
README.md pour la justification : budget ~10 min, tooling Angular trop lourd).

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `app.js` — tableau `QUESTIONS`, logique de sélection de réponse (`selectAnswer`),
  progression (`nextQuestion`), écran résultat (`showResult`), `restart`.
- `index.html` — deux écrans togglables : `quizScreen` / `resultScreen`.
- `style.css` — carte centrée, couleurs correct/incorrect.

## Notes
- Aucune persistance : le score repart de zéro à chaque `restart()` ou rechargement.
