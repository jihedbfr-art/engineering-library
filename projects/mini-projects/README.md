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
| [typing-speed-test](typing-speed-test) | Vanilla JS | Test de vitesse de frappe (MPM + précision) |
| [rock-paper-scissors-game](rock-paper-scissors-game) | Vanilla JS | Pierre-feuille-ciseaux vs machine |
| [hangman-game-js](hangman-game-js) | Vanilla JS | Jeu du pendu, clavier virtuel |
| [dice-rpg-roller](dice-rpg-roller) | Vanilla JS | Lanceur de dés notation RPG (NdM+B) |
| [markdown-live-preview](markdown-live-preview) | Vanilla JS | Éditeur Markdown, preview live |
| [regex-playground-app](regex-playground-app) | Vanilla JS | Testeur de regex avec surlignage |
| [mini-key-value-store-server](mini-key-value-store-server) | Java (HttpServer) | Store clé-valeur HTTP GET/PUT/DELETE |
| [file-encryption-tool-aes](file-encryption-tool-aes) | Java | Chiffrement AES-256-CBC + PBKDF2 |
| [color-contrast-checker](color-contrast-checker) | Vanilla JS | Ratio de contraste WCAG 2.1 |
| [invoice-generator-app](invoice-generator-app) | Python | Génère une facture texte formatée |
| [json-schema-validator-app](json-schema-validator-app) | Python | Mini validateur JSON Schema |
| [mini-search-engine-inverted-index](mini-search-engine-inverted-index) | Java | Moteur de recherche à index inversé |
| [presence-tracker-websocket](presence-tracker-websocket) | Python | Suivi de présence (heartbeat) |
| [mini-log-viewer-webapp](mini-log-viewer-webapp) | Vanilla JS | Visualiseur de logs, filtre par niveau |
| [group-expense-splitter](group-expense-splitter) | Vanilla JS | Partage de dépenses de groupe |
| [simple-survey-builder](simple-survey-builder) | Vanilla JS | Créateur de sondage, export JSON |
| [streak-counter-app](streak-counter-app) | Vanilla JS | Compteur de série quotidienne |
| [code-snippet-organizer](code-snippet-organizer) | Vanilla JS | Bibliothèque de snippets de code |
| [simple-analytics-pixel-counter](simple-analytics-pixel-counter) | Java (HttpServer) | Pixel de tracking analytics |
| [simple-rss-feed-reader](simple-rss-feed-reader) | Python | Parseur de flux RSS/XML local |
| [inventory-barcode-lookup-stub](inventory-barcode-lookup-stub) | Java | Validation EAN-13 + lookup catalogue |
| [simple-feature-toggle-dashboard](simple-feature-toggle-dashboard) | Vanilla JS | Dashboard de feature flags |
| [simple-cron-scheduler-daemon](simple-cron-scheduler-daemon) | Python | Scheduler in-process multi-jobs |
| [simple-blog-comment-moderation-queue](simple-blog-comment-moderation-queue) | Java | File de modération de commentaires |
| [mini-url-shortener-webapp](mini-url-shortener-webapp) | Vanilla JS | Raccourcisseur d'URL base62 |
| [unit-test-runner-mini](unit-test-runner-mini) | Python | Mini framework de tests |
| [simple-note-encryption-app](simple-note-encryption-app) | Vanilla JS | Notes chiffrées (Web Crypto API) |
| [workout-log-tracker](workout-log-tracker) | Vanilla JS | Journal de musculation |
| [water-intake-tracker](water-intake-tracker) | Vanilla JS | Suivi d'hydratation quotidien |
| [recipe-cost-calculator](recipe-cost-calculator) | Python | Coût d'une recette par portion |
| [flashcard-spaced-repetition](flashcard-spaced-repetition) | Vanilla JS | Flashcards, répétition espacée (SM-2) |
| [simple-blog-static-generator](simple-blog-static-generator) | Python | Générateur de site statique (Markdown) |
| [live-vote-counter](live-vote-counter) | Vanilla JS | Sondage avec barres en direct |
| [kanban-board-vanilla-js](kanban-board-vanilla-js) | Vanilla JS | Kanban drag & drop natif HTML5 |
| [qr-code-generator-ui](qr-code-generator-ui) | Vanilla JS | Motif visuel déterministe type QR |
| [password-strength-meter-ui](password-strength-meter-ui) | Vanilla JS | Mesureur de force de mot de passe |
| [simple-chatbot-rules-based](simple-chatbot-rules-based) | Vanilla JS | Chatbot à règles regex (pas de LLM) |
| [text-diff-viewer-html](text-diff-viewer-html) | Vanilla JS | Diff ligne par ligne, surlignage |
| [minesweeper-lite](minesweeper-lite) | Vanilla JS | Démineur 8x8, flood fill |
| [connect-four-game](connect-four-game) | Vanilla JS | Puissance 4, détection victoire 4 sens |
| [whack-a-mole-game](whack-a-mole-game) | Vanilla JS | Chasse-taupe chronométré |
| [drawing-pad-canvas](drawing-pad-canvas) | Vanilla JS (canvas) | Bloc de dessin libre, souris + tactile |
| [color-palette-generator-app](color-palette-generator-app) | Vanilla JS | Palette de couleurs harmonieuses (HSL) |
| [countdown-event-tracker](countdown-event-tracker) | Vanilla JS | Comptes à rebours multi-événements |
| [unit-price-comparator](unit-price-comparator) | Vanilla JS | Comparateur de prix au gramme/ml |
| [ini-config-editor-webapp](ini-config-editor-webapp) | Vanilla JS | Éditeur .ini, aperçu JSON live |
| [http-request-builder-webapp](http-request-builder-webapp) | Vanilla JS | Génère curl/fetch depuis un formulaire |
| [simple-changelog-generator](simple-changelog-generator) | Python | CHANGELOG depuis les commits git |
| [personal-dashboard-widgets](personal-dashboard-widgets) | Vanilla JS | Mini dashboard (horloge, todo, citation) |
| [simple-poker-hand-evaluator](simple-poker-hand-evaluator) | Java | Évalue une main de poker à 5 cartes |

## Règles
- Les noms `*-angular` et `*-spring` désignent l'intention d'origine ; l'implémentation réelle
  (vanilla JS / HttpServer) et la raison du choix sont documentées dans le `README.md` de chaque projet.
