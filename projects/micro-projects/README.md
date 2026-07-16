# 🔧 Micro-projects

25 utilitaires simples, un dossier par outil. Chacun a son `README.md` (usage) et son `CLAUDE.md`
(stack, commandes). Zéro dépendance réseau : Python stdlib ou Java avec `com.sun.net.httpserver.HttpServer`
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

## Règles
- Aucune opération git sur ce PC (voir `CLAUDE.md` racine).
- Chaque outil est autonome, sans build tool lourd (pas de Maven/npm avec dépendances réseau).
