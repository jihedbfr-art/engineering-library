# Quiz App

## But
Quiz à choix multiples de 10 questions codées en dur, feedback correct/incorrect
immédiat par question, score final.

## Mini-architecture
- `index.html` : écran quiz + écran résultat.
- `style.css` : mise en page carte centrée.
- `app.js` : tableau `QUESTIONS` (question, options, index de la bonne réponse),
  machine à états simple (`current`, `score`, `answered`).

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique — écart par rapport au nom du dossier
Le dossier s'appelle `quiz-app-angular` mais le budget du mini-projet (~10 min de dev)
ne permet pas d'installer une vraie CLI Angular (tooling, compilation, node_modules).
**Implémentation en Vanilla JS/HTML/CSS**, zéro dépendance, zéro build — même résultat
fonctionnel (composant quiz, état, rendu réactif) sans le poids d'Angular.
