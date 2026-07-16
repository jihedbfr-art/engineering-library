# 🧩 Mini-projects

25 petites apps, un dossier par projet. Chacun a son `README.md` (but, archi, lancement) et son
`CLAUDE.md`. Choix technique volontaire pour rester dans un budget ~10 min/projet : vanilla
JS/HTML/CSS à la place d'Angular CLI complet, `com.sun.net.httpserver.HttpServer` à la place de
Spring Boot/Maven (pas de dépendance réseau garantie). Ces écarts sont documentés dans chaque projet.

| Projet | Stack | Rôle |
|---|---|---|
| [todo-app-angular](todo-app-angular) | Vanilla JS | Todo list (localStorage) |
| [notes-app-spring](notes-app-spring) | Java (HttpServer) | CRUD de notes (fichier JSON) |
| [pastebin-clone](pastebin-clone) | Java (HttpServer) | Pastebin minimal |
| [url-shortener-app](url-shortener-app) | Java (HttpServer) | Raccourcisseur d'URL avec redirection |
| [weather-dashboard-angular](weather-dashboard-angular) | Vanilla JS | Dashboard météo (données simulées) |
| [kanban-board-angular](kanban-board-angular) | Vanilla JS | Kanban drag & drop (localStorage) |
| [expense-tracker](expense-tracker) | Vanilla JS | Suivi de dépenses + graphique CSS |
| [recipe-book-app](recipe-book-app) | Vanilla JS | Carnet de recettes (localStorage) |
| [blog-engine-spring](blog-engine-spring) | Java (HttpServer) | Blog à partir de fichiers Markdown |
| [chat-app-websocket](chat-app-websocket) | Java (HttpServer) | Chat en long-polling (pas de vrai WS) |
| [polling-voting-app](polling-voting-app) | Java (HttpServer) | Sondages + résultats en % |
| [bookmark-manager](bookmark-manager) | Vanilla JS | Favoris + tags (localStorage) |
| [habit-tracker](habit-tracker) | Vanilla JS | Suivi d'habitudes, heatmap 30 jours |
| [shopping-cart-demo](shopping-cart-demo) | Vanilla JS | Panier d'achat (sans paiement réel) |
| [quiz-app-angular](quiz-app-angular) | Vanilla JS | Quiz à choix multiples |
| [library-management-mini](library-management-mini) | Java (HttpServer) | CRUD de livres |
| [inventory-tracker](inventory-tracker) | Vanilla JS | Stock + alerte seuil bas |
| [contact-manager-crud](contact-manager-crud) | Vanilla JS | CRUD de contacts |
| [pomodoro-timer-angular](pomodoro-timer-angular) | Vanilla JS | Timer Pomodoro 25/5 |
| [markdown-notes-editor](markdown-notes-editor) | Vanilla JS | Éditeur Markdown avec preview live |
| [tic-tac-toe-angular](tic-tac-toe-angular) | Vanilla JS | Morpion 2 joueurs |
| [snake-game-js](snake-game-js) | Vanilla JS (canvas) | Jeu Snake |
| [memory-card-game](memory-card-game) | Vanilla JS | Jeu de memory |
| [mood-tracker-app](mood-tracker-app) | Vanilla JS | Suivi d'humeur, calendrier CSS |
| [job-board-listing-app](job-board-listing-app) | Vanilla JS | Liste d'offres d'emploi + filtres |

## Règles
- Aucune opération git sur ce PC (voir `CLAUDE.md` racine).
- Les noms `*-angular` et `*-spring` désignent l'intention d'origine ; l'implémentation réelle
  (vanilla JS / HttpServer) et la raison du choix sont documentées dans le `README.md` de chaque projet.
