# Pomodoro Timer

## But
Timer pomodoro classique : 25 min de travail / 5 min de pause, démarrer/pause/reset,
compteur de sessions de travail complétées, bip sonore à chaque transition.

## Mini-architecture
- `index.html` : affichage du temps + mode + boutons.
- `style.css` : carte centrée, chiffres larges.
- `app.js` : `setInterval` d'une seconde, bascule travail/pause automatique à 0,
  bip généré via Web Audio API (`playBeep`, oscillateur, pas de fichier audio).

## Lancer
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Choix technique — écart par rapport au nom du dossier
Le dossier s'appelle `pomodoro-timer-angular` mais pour un budget de ~10 min de dev,
une vraie CLI Angular (tooling, compilation) est disproportionnée pour un timer à
un seul écran. **Implémentation en Vanilla JS/HTML/CSS**, zéro dépendance. Le son est
généré avec l'API Web Audio native (`OscillatorNode`) plutôt qu'un fichier `.mp3`
externe, pour rester zéro dépendance.
