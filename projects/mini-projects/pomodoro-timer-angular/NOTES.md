# pomodoro-timer-angular

Timer pomodoro 25/5 min avec compteur de sessions et bip sonore.

## Stack
Vanilla JS/HTML/CSS — **PAS de vraie CLI Angular** (voir README.md). Son via Web
Audio API native, pas de fichier audio.

## Commandes
Ouvrir `index.html` dans un navigateur, ou `npx serve .`.

## Fichiers clés
- `app.js` — `tick()` décrémente chaque seconde, bascule `isWork` à 0, `playBeep()`
  joue un bip via `OscillatorNode`, `start`/`pause`/`reset` pilotent `setInterval`.
- `index.html` — affichage temps + mode + boutons.
- `style.css` — carte centrée sur fond sombre.

## Notes
- Aucune persistance : le compteur de sessions repart de zéro au rechargement.
- `WORK_SECONDS` / `BREAK_SECONDS` en haut de `app.js` pour ajuster les durées.
