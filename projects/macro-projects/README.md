# 🏢 Macro-projects (application d'entreprise / large-scale)

Palier 5 de l'échelle nano → micro → mini → standard → macro → plateforme (racine `projects/`).

Des applications à plusieurs modules/couches (ex. couche service + couche persistance +
API + un peu d'auth), avec une vraie persistance et plus d'un cas d'usage bout-en-bout — mais
encore une seule architecture cohérente, pas un écosystème multi-services. Le palier au-dessus
(plateforme/écosystème) est déjà représenté par les projets vitrine à la racine de `projects/`
(ex. `notes-app-microservices/`), qui démontrent chacun une architecture différente et ne sont pas
soumis à un budget de temps.

Budget indicatif : ~1 à 3 jours par projet (contre ~1-3h pour `standard-projects/`).

Chaque projet vit dans son propre dossier kebab-case, avec un `README.md` (but, archi, lancement)
et un `CLAUDE.md` (stack, commandes, fichiers clés, règles spécifiques) — ce dernier reste local,
jamais commité (voir `.gitignore` racine).

## Règles
- Une seule architecture par projet, mais on peut viser une vraie couverture fonctionnelle
  (plusieurs modules, plusieurs rôles utilisateurs, etc.).

_(Dossier vide pour l'instant — à remplir au fil de l'eau.)_
