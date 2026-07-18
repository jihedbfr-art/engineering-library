# 🔧 Micro-projects

25 utilitaires simples, un dossier par outil. Chacun a son `README.md` (usage) et son `CLAUDE.md`
(stack, commandes) — ce dernier reste local, jamais commité (voir `.gitignore` racine). Zéro
dépendance réseau : Python stdlib ou Java avec `com.sun.net.httpserver.HttpServer`
(pas de Spring Boot/Maven pour ces micro-outils).

| Projet | Stack | Rôle |
|---|---|---|
| [password-generator-cli](password-generator-cli) | Python | Génère un mot de passe aléatoire sécurisé |
| [json-pretty-printer-cli](json-pretty-printer-cli) | Python | Réindente un JSON proprement |
| [markdown-to-html-cli](markdown-to-html-cli) | Python | Convertit un sous-ensemble Markdown en HTML |
| [qr-code-generator-cli](qr-code-generator-cli) | Python | QR code (ou fallback ASCII si `qrcode` absent) |
| [csv-to-json-cli](csv-to-json-cli) | Python | Convertit un CSV en JSON |
| [word-counter-cli](word-counter-cli) | Python | Compte mots/lignes/caractères, top mots |
| [palindrome-checker-api](palindrome-checker-api) | Java (HttpServer) | API GET /check?text= |
| [ip-validator-cli](ip-validator-cli) | Python | Valide IPv4/IPv6, type d'adresse |
| [base64-encoder-cli](base64-encoder-cli) | Python | Encode/décode Base64 |
| [regex-tester-cli](regex-tester-cli) | Python | Teste un pattern regex sur un texte |
| [todo-list-cli](todo-list-cli) | Python | Todo list persistée en JSON local |
| [currency-converter-cli](currency-converter-cli) | Python | Conversion de devises (taux statiques) |
| [calculator-rest-api](calculator-rest-api) | Java (HttpServer) | API GET /calculate?op= |
| [duplicate-file-finder-cli](duplicate-file-finder-cli) | Python | Trouve les fichiers dupliqués (hash SHA-256) |
| [hash-generator-cli](hash-generator-cli) | Python | MD5/SHA1/SHA256/SHA512 |
| [fake-data-generator-cli](fake-data-generator-cli) | Python | Génère de faux utilisateurs (JSON/CSV) |
| [unit-converter-cli](unit-converter-cli) | Python | Longueur/poids/température |
| [text-case-converter-cli](text-case-converter-cli) | Python | camelCase/snake_case/kebab-case/PascalCase |
| [lru-cache-demo](lru-cache-demo) | Java | LRU Cache maison (HashMap + liste chaînée) |
| [jwt-decoder-cli](jwt-decoder-cli) | Python | Décode un JWT (sans vérifier la signature) |
| [log-file-analyzer-cli](log-file-analyzer-cli) | Python | Stats sur un log Apache/Nginx |
| [port-scanner-cli](port-scanner-cli) | Python | Scan de ports, limité à localhost |
| [luhn-validator-api](luhn-validator-api) | Java (HttpServer) | Valide un format de numéro (algo Luhn) |
| [slug-generator-cli](slug-generator-cli) | Python | Texte → slug URL-friendly |
| [url-shortener-core](url-shortener-core) | Java | Logique cœur raccourcisseur d'URL (base62) |
| [subnet-calculator-cli](subnet-calculator-cli) | Python | Calcul de sous-réseau IPv4 (CIDR) |
| [iban-validator-cli](iban-validator-cli) | Java | Validation IBAN (modulo 97, ISO 13616) |
| [word-frequency-counter-cli](word-frequency-counter-cli) | Python | Fréquence des mots dans un texte |
| [levenshtein-distance-cli](levenshtein-distance-cli) | Java | Distance d'édition entre deux chaînes |
| [jwt-generator-cli](jwt-generator-cli) | Python | Génère un JWT signé HS256 |
| [json-diff-cli](json-diff-cli) | Python | Diff structurel entre deux JSON |
| [timezone-converter-cli](timezone-converter-cli) | Java | Conversion entre fuseaux horaires |
| [markdown-toc-generator-cli](markdown-toc-generator-cli) | Python | Table des matières depuis un Markdown |
| [csv-column-stats-cli](csv-column-stats-cli) | Python | Stats descriptives d'une colonne CSV |
| [url-encoder-decoder-cli](url-encoder-decoder-cli) | Java | Encode/décode URL |
| [duplicate-lines-remover-cli](duplicate-lines-remover-cli) | Python | Supprime les lignes dupliquées |
| [mac-address-vendor-lookup-stub](mac-address-vendor-lookup-stub) | Python | Constructeur depuis l'OUI d'une MAC |
| [http-status-code-explainer-api](http-status-code-explainer-api) | Java (HttpServer) | Explique un code de statut HTTP |
| [binary-search-cli](binary-search-cli) | Python | Recherche dichotomique |
| [ini-file-parser-cli](ini-file-parser-cli) | Python | Parse un .ini en JSON |
| [text-statistics-cli](text-statistics-cli) | Java | Mots/phrases/temps de lecture d'un texte |
| [csv-merge-cli](csv-merge-cli) | Python | Fusionne plusieurs CSV |
| [base32-encoder-cli](base32-encoder-cli) | Python | Encode/décode Base32 (RFC 4648) |
| [xor-cipher-cli](xor-cipher-cli) | Python | Chiffrement XOR répété |
| [topological-sort-cli](topological-sort-cli) | Java | Tri topologique (Kahn) |
| [dijkstra-shortest-path-cli](dijkstra-shortest-path-cli) | Python | Plus court chemin pondéré |
| [huffman-encoding-cli](huffman-encoding-cli) | Python | Compression Huffman |
| [sudoku-validator-api](sudoku-validator-api) | Java | Valide une grille de sudoku 9x9 |
| [rpn-calculator-cli](rpn-calculator-cli) | Python | Calculatrice notation polonaise inversée |
| [anagram-group-finder-cli](anagram-group-finder-cli) | Java | Regroupe les mots anagrammes |
| [rate-limiter-demo-api](rate-limiter-demo-api) | Python | Rate limiter sliding window |
| [lru-cache-api](lru-cache-api) | Java | Cache LRU via LinkedHashMap access-order |
| [state-machine-demo-cli](state-machine-demo-cli) | Python | Machine à états (cycle de vie commande) |
| [simple-graph-bfs-dfs-cli](simple-graph-bfs-dfs-cli) | Python | Parcours BFS et DFS côte à côte |
| [circuit-breaker-demo-api](circuit-breaker-demo-api) | Java | Circuit breaker CLOSED/OPEN/HALF_OPEN |
| [token-bucket-limiter-cli](token-bucket-limiter-cli) | Python | Rate limiting par seau de jetons |
| [outbox-pattern-demo-cli](outbox-pattern-demo-cli) | Python | Transactional outbox (SQLite) |
| [idempotency-key-store-api](idempotency-key-store-api) | Java | Store d'idempotence pour retries |
| [retry-with-backoff-cli](retry-with-backoff-cli) | Python | Retry exponential backoff + jitter |
| [e164-phone-formatter-cli](e164-phone-formatter-cli) | Python | Numéro local vers format E.164 |
| [structured-log-parser-cli](structured-log-parser-cli) | Java | Compte les logs par niveau |
| [health-check-aggregator-api](health-check-aggregator-api) | Python | Endpoint /health façon Actuator |
| [feature-flag-toggle-api](feature-flag-toggle-api) | Java | Feature flags en mémoire |
| [consistent-hashing-demo-cli](consistent-hashing-demo-cli) | Python | Consistent hashing à noeuds virtuels |
| [bloom-filter-cli](bloom-filter-cli) | Python | Filtre de Bloom probabiliste |
| [saga-orchestrator-demo-cli](saga-orchestrator-demo-cli) | Python | Saga avec compensation par étape |
| [cron-expression-explainer-cli](cron-expression-explainer-cli) | Java | Explique une expression cron 5 champs |
| [simple-templating-engine-cli](simple-templating-engine-cli) | Python | Mini moteur `{{ variable }}` |
| [write-ahead-log-demo-cli](write-ahead-log-demo-cli) | Java | Write-ahead log rejouable |
| [sliding-window-log-cli](sliding-window-log-cli) | Python | Médiane glissante sur série temporelle |
| [read-through-cache-demo-cli](read-through-cache-demo-cli) | Python | Cache read-through avec loader |
| [leaky-bucket-limiter-cli](leaky-bucket-limiter-cli) | Java | Rate limiting par seau percé |
| [two-factor-totp-cli](two-factor-totp-cli) | Python | Génère un code TOTP (RFC 6238) |
| [api-versioning-router-cli](api-versioning-router-cli) | Java | Routeur multi-version d'API |
| [graceful-shutdown-demo-cli](graceful-shutdown-demo-cli) | Python | Arrêt propre sur SIGTERM/SIGINT |

## Règles
- Chaque outil est autonome, sans build tool lourd (pas de Maven/npm avec dépendances réseau).
- Langage libre par projet (Python stdlib ou Java `com.sun.net.httpserver.HttpServer`) — pas de Spring Boot ici.
