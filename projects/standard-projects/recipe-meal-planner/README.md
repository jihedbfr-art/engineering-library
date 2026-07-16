# recipe-meal-planner

Planificateur de repas hebdomadaire : bibliothèque de recettes réutilisables, planning sur 7 jours
(petit-déjeuner / déjeuner / dîner), et génération automatique d'une liste de courses agrégée à
partir du planning de la semaine.

## Architecture

Backend Java pur (`com.sun.net.httpserver.HttpServer`, zéro dépendance externe) exposant une API
REST JSON. Un mini-parseur/writer JSON maison (`Json.java`) sert au lieu d'une librairie. Le
frontend est du HTML/CSS/JS vanilla servi statiquement par le même serveur. Persistance sur deux
fichiers JSON (`data/recipes.json`, `data/planning.json`), verrouillage par un `ReentrantLock` pour
sérialiser les écritures.

## Lancement

```bash
cd recipe-meal-planner
javac -d out src/*.java
java -cp out Server 8080
```

Puis ouvrir http://localhost:8080. Le port par défaut est 8080 (premier argument optionnel).

## API

- `GET /api/recipes` / `POST /api/recipes` / `PUT /api/recipes/{id}` / `DELETE /api/recipes/{id}`
- `GET /api/planning` / `PUT /api/planning` (mise à jour partielle par jour/repas)
- `GET /api/shopping-list` — additionne les ingrédients (nom + unité) de toutes les recettes
  planifiées dans la semaine

## Limitations connues

- Une seule semaine de planning (pas d'historique multi-semaines, pas de dates réelles).
- L'agrégation de la liste de courses est purement textuelle : "g" et "kg" ne sont pas convertis
  entre eux, un même ingrédient avec deux unités différentes donnera deux lignes.
- Pas d'authentification — usage mono-utilisateur local.
- Pas de tests automatisés ; vérifié manuellement via curl (CRUD recettes, planning, agrégation,
  suppression en cascade).
