# 🔬 Nano-projects

Palier 1 de l'échelle nano → micro → mini → standard → macro → plateforme (racine `projects/`).

Le cran en dessous de `micro-projects/` : un one-liner ou un snippet exécutable, une seule
fonction, pas d'options ni de gestion d'erreurs élaborée. Budget indicatif : < 5 min par projet.

Chaque projet vit dans son propre dossier kebab-case, avec un `README.md` court (ce que ça fait,
comment le lancer) et éventuellement un `CLAUDE.md` de quelques lignes si utile — ce dernier reste
local, jamais commité (voir `.gitignore` racine).

## Règles
- Vraiment minimal : si ça dépasse ~30 lignes de code, ça devrait être un `micro-project`, pas un nano.
- Langage libre par projet (Python, JavaScript/Node, Java) — la contrainte est la taille, pas la stack.

## Projets

- `is-even-odd` — pair ou impair.
- `fizzbuzz` — FizzBuzz de 1 à N.
- `reverse-string` — inverse une chaîne.
- `factorial-calculator` — factorielle d'un nombre.
- `leap-year-checker` — année bissextile.
- `celsius-to-fahrenheit` — conversion de température.
- `random-number-picker` — nombre aléatoire dans une plage.
- `vowel-counter` — compte les voyelles.
- `list-flattener` — aplatit une liste imbriquée.
- `array-shuffle` — mélange une liste.
- `temperature-emoji` — emoji selon la température.
- `coin-flip-simulator` — lancers de pièce pile/face.
- `dice-roller` — lancer de dés.
- `string-capitalizer` — capitalise chaque mot.
- `simple-stopwatch` — chronomètre start/stop.
- `gcd-lcm-calculator` — PGCD et PPCM.
- `collatz-sequence` — suite de Syracuse.
- `digital-root` — racine numérique.
- `is-armstrong-number` — nombre d'Armstrong.
- `hex-to-rgb` — couleur hex vers RGB.
- `rgb-to-hex` — RGB vers couleur hex.
- `rot13-cipher` — chiffrement ROT13.
- `imei-luhn-check` — validation IMEI par l'algorithme de Luhn.
- `sim-iccid-length-check` — forme d'un ICCID de carte SIM.
- `mcc-mnc-lookup-stub` — mini-annuaire opérateur mobile par code MCC-MNC.
- `imsi-length-validator` — longueur d'un IMSI.
- `day-of-week-finder` — jour de la semaine d'une date.
- `is-weekend-checker` — samedi/dimanche ou non.
- `countdown-to-date` — jours restants jusqu'à une date.
- `run-length-encode` — compression RLE.
- `run-length-decode` — décompression RLE.
- `is-happy-number` — nombre "heureux".
- `is-perfect-number` — nombre parfait.
- `is-isogram-checker` — lettres toutes différentes.
- `is-pangram-checker` — contient les 26 lettres.
- `longest-word-finder` — mot le plus long d'une phrase.
- `shortest-word-finder` — mot le plus court d'une phrase.
- `char-histogram-ascii` — histogramme ASCII de fréquence des lettres.
- `camel-to-snake-case` — camelCase vers snake_case.
- `snake-to-camel-case` — snake_case vers camelCase.
- `morse-to-text` — décodage morse vers texte.
- `dna-complement` — brin complémentaire d'ADN.
- `rna-transcription` — transcription ADN vers ARN.
- `is-valid-parentheses-nano` — équilibre de parenthèses/crochets/accolades.
- `caesar-cipher-bruteforce` — les 26 décalages Cesar possibles.
- `matrix-transpose` — transposée d'une matrice.
- `matrix-identity-generator` — matrice identité N×N.
- `quadratic-roots` — racines réelles d'une équation du second degré.
- `fibonacci-nth-fast` — n-ième Fibonacci, itératif.
- `sum-of-primes-below-n` — somme des nombres premiers < N.
- `binary-to-decimal` — binaire vers décimal.
- `decimal-to-binary` — décimal vers binaire.
- `gray-code-converter` — binaire vers code Gray.
- `roman-to-decimal` — chiffres romains vers décimal.
- `number-to-words` — nombre en toutes lettres (0-99, FR).
- `random-hex-color-generator` — couleur hex aléatoire.
- `random-color-name-picker` — nom de couleur aléatoire.
- `random-name-generator-nano` — nom aléatoire (prénom + nom).
- `random-password-pin` — code PIN numérique aléatoire sécurisé.
- `temperature-kelvin-converter` — Celsius ↔ Kelvin.
- `bmi-category-nano` — catégorie OMS à partir d'un IMC.
- `is-valid-luhn-card-nano` — check-digit de Luhn générique.
- `is-triangle-valid` — inégalité triangulaire.
- `tic-tac-toe-winner-checker` — détection du gagnant d'un morpion.
- `binary-clock-text` — horloge binaire textuelle.
