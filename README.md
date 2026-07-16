# 📚 Engineering Library

> Le but n'est pas qu'on dise « il a 120 repositories ».
> Le but est qu'on dise « **ce GitHub est une bibliothèque d'ingénierie** ».

Écosystème d'ingénierie de **Jihed** — développeur Java / Spring.
Le code est 20 % du contenu ; les 80 % restants sont du savoir réutilisable : architectures, décisions,
échecs, recettes, playbooks, checklists. Pensé pour rester utile dans 10 ans, à un étudiant comme à un CTO.

## Organisation

```
jihedapps/
├── projects/                  # Échelle nano → micro → mini → standard → macro → plateforme
│   ├── nano-projects/               < 5 min — one-liner/snippet, à remplir au fil de l'eau
│   ├── micro-projects/              ~10 min — 25 utilitaires CLI/API (Python stdlib, Java HttpServer)
│   ├── mini-projects/               ~10-15 min — 25 petites apps (vanilla JS, Java HttpServer)
│   ├── standard-projects/           ~1-3h — app standard, à remplir au fil de l'eau
│   ├── macro-projects/              ~1-3j — application d'entreprise, à remplir au fil de l'eau
│   └── notes-app-microservices/     plateforme/écosystème — Spring Boot 3.2.5 · Angular 17 · Keycloak · Kafka · MinIO · Eureka
│
├── knowledge/                 # 80 % — la vraie bibliothèque
│   ├── architecture-library/      monolith · microservices · event-driven · cqrs · hexagonal · ddd · saga…
│   ├── engineering-decisions/     ADR : « Pourquoi PostgreSQL ? », « Pourquoi Keycloak ? »…
│   ├── engineering-failures/      memory leak · deadlock · N+1 · cache stampede · OOM…
│   ├── debugging-recipes/         « Spring ne démarre pas », « Kafka consumer stuck »…
│   ├── performance-recipes/       SQL · JVM · GC · Hibernate · Redis · Kafka…
│   ├── security-patterns/         JWT · OAuth2 · Keycloak · RBAC · OWASP Top 10…
│   ├── api-design-guide/          REST · GraphQL · gRPC · pagination · idempotence…
│   ├── database-engineering/      Postgres · Oracle · Mongo · Redis · index · MVCC · réplication…
│   ├── ai-engineering/            hooks · prompts · agents · MCP · RAG · évaluation…
│   ├── engineering-playbooks/     procédures 2h du matin : incident prod · rollback · migration…
│   ├── engineering-checklists/    avant merge · avant prod · avant migration BDD…
│   ├── code-review-guide/         comment reviewer, smells, anti-patterns…
│   └── engineering-cookbook/      « Je veux un JWT / Kafka / Circuit Breaker » → recette
│
├── docs/standards/            # Conventions transverses (Java/Spring, Angular, SQL, sécurité)
└── .claude/                   # Config Claude Code (skills + CLAUDE.md)
```

## Comment ça grandit

Chaque dossier de `knowledge/` contient :
- un **README** qui explique son rôle et indexe ses entrées ;
- un **`_TEMPLATE.md`** — le moule que toute nouvelle entrée doit suivre (homogénéité garantie) ;
- des **exemples réels** ancrés dans les projets, pas du remplissage.

On ne crée pas 40 pages vides d'un coup : on remplit **au fil de l'eau**, une entrée quand on la vit vraiment.

## Cap à 3 ans

Un centre de connaissances, pas un tas de repos : projets démonstratifs à archis variées, ADR réutilisables,
bibliothèque d'échecs et de recettes, playbooks et checklists opérationnels.
