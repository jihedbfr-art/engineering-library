# Checklist — Avant merge

> À passer avant de fusionner une branche dans la principale.

## Build & tests
- [ ] `mvn verify` passe en local (backend) — voir [docs/standards/java-spring.md](../../docs/standards/java-spring.md).
- [ ] `npm run build` + `npm test` passent (frontend).
- [ ] Aucun test ignoré (`@Disabled` / `xit`) laissé sans justification.

## Qualité
- [ ] Pas de `System.out.println` / `console.log` de debug oubliés.
- [ ] Pas de secret en dur (clé, mot de passe, token) — grep avant de pousser.
- [ ] Pas de N+1 introduit sur les endpoints listant ([failures/hibernate-n-plus-1.md](../engineering-failures/hibernate-n-plus-1.md)).

## Sécurité
- [ ] Nouveaux endpoints protégés par défaut (authenticated) sauf exception documentée.
- [ ] Entrées utilisateur validées (`@Valid` / DTO), pas de requête SQL concaténée.

## Diff
- [ ] Le diff ne contient que le sujet de la PR (pas de reformatage massif parasite).
- [ ] Une entrée [engineering-decisions](../engineering-decisions/) est créée si un choix d'archi a été fait.

## 🚫 Bloquant
Build rouge · secret en dur · endpoint ouvert non documenté → **on ne merge pas**.
