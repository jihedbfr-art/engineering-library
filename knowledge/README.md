# knowledge/ — La bibliothèque de savoir

Les 80 % de l'écosystème. Ici on réfléchit plus qu'on ne code. Chaque dossier est un domaine ;
chaque fichier est **une** entrée autonome qui suit le `_TEMPLATE.md` de son domaine (là où il
existe — les domaines encyclopédiques ci-dessous suivent le format libre hérité de leur origine).

## Domaines de pratique (liés aux projets de ce dépôt)

| Domaine | Ce qu'on y met | État |
|---|---|---|
| [architecture-library](architecture-library/) | Styles d'architecture : pourquoi, quand (pas), trade-offs, diagrammes | 2 exemples (microservices, event-driven vs request-response) |
| [engineering-decisions](engineering-decisions/) | ADR numérotés : « Pourquoi X ? » avec contexte, options, conséquences | 3 ADR (Keycloak, PostgreSQL, JHipster) |
| [engineering-failures](engineering-failures/) | Pannes vécues : cause, symptômes, diagnostic, solution, prévention | 2 exemples (Hibernate N+1, Kafka consumer lag) |
| [debugging-recipes](debugging-recipes/) | « Ça ne marche pas » → checklist de diagnostic ciblée | 2 exemples (LazyInitializationException, pool épuisé) |
| [performance-recipes](performance-recipes/) | Optimisations SQL / JVM / GC / Hibernate / cache / Kafka | 1 exemple (dimensionnement HikariCP) |
| [security-patterns](security-patterns/) | JWT, OAuth2, Keycloak, RBAC, OWASP Top 10 | 2 exemples (OAuth2/Keycloak, anti-bruteforce) |
| [api-design-guide](api-design-guide/) | REST, GraphQL, gRPC, versioning, pagination, idempotence | 1 exemple (pagination) |
| [database-engineering](database-engineering/) | Par moteur : index, transactions, isolation, MVCC, réplication | 2 moteurs (PostgreSQL, Oracle) |
| [ai-engineering](ai-engineering/) | Hooks, prompts, agents, MCP, RAG, évaluation, context engineering | squelette (37 sous-domaines) + 26 fiches réelles |
| [engineering-playbooks](engineering-playbooks/) | Procédures « 2h du matin » : incident, rollback, migration | 1 exemple (rollback de déploiement) |
| [engineering-checklists](engineering-checklists/) | Avant merge / prod / migration / release | 2 exemples (before-merge, avant mise en prod) |
| [code-review-guide](code-review-guide/) | Comment reviewer : smells, anti-patterns, perf, tests, sécurité | 1 exemple (détecter un N+1) |
| [engineering-cookbook](engineering-cookbook/) | « Je veux faire X » → recette prête à l'emploi | 2 exemples (JWT resource server, Kafka producer/consumer) |

## Domaines encyclopédiques (absorbés depuis l'ancien dépôt dev-library)

Contenu généraliste développeur/IT, un fichier = un article, format libre (pas de `_TEMPLATE.md`
imposé). Ancien dépôt `dev-library` archivé — tout le contenu vit désormais ici.

| Domaine | Ce qu'on y trouve |
|---|---|
| [computer-science](computer-science/) | Big-O, structures de données, algorithmes, design patterns, system design |
| [programming](programming/) | Python, JS/TS, Java, Go, Rust, clean code, concurrence par langage |
| [web](web/) | Fonctionnement du web, HTTP, REST vs GraphQL, rendu navigateur |
| [networking](networking/) | TCP/IP, DNS, ports, outils de diagnostic |
| [backend](backend/) | Java/Spring, Node.js, patterns API, microservices |
| [frontend](frontend/) | Angular, React, fondamentaux web |
| [databases](databases/) | SQL, NoSQL, modélisation, performance (complète `database-engineering`) |
| [cloud](cloud/) | Modèles de service, AWS/Azure/GCP essentiels |
| [devsecops](devsecops/) | CI/CD, conteneurs, sécurité, IaC, monitoring, GitOps |
| [cybersecurity](cybersecurity/) | Fondamentaux, sécurité web, blue team, parcours d'apprentissage |
| [ai-reference](ai-reference/) | LLM, RAG, prompt engineering, agents, évaluation (angle encyclopédique, complète `ai-engineering`) |
| [telecom](telecom/) | Réseaux mobiles, 5G, protocoles, OSS/BSS, billing, opérateurs, IoT — ancré dans l'expérience réelle |
| [mobile](mobile/) | Android, iOS, Flutter, React Native |
| [data-engineering](data-engineering/) | Pipelines ETL/ELT, streaming Kafka, entrepôts, qualité des données |
| [sre](sre/) | SLI/SLO/error budgets, gestion d'incident, capacity planning |
| [legacy-modernization](legacy-modernization/) | Strangler fig, playbook de migration, legacy non testé, Oracle ADF |
| [game-dev](game-dev/) | Boucle de jeu, ECS, pipeline de rendu, netcode multijoueur |
| [embedded-iot](embedded-iot/) | Microcontrôleurs, RTOS/inversion de priorité, MQTT/CoAP |
| [blockchain](blockchain/) | Consensus, smart contracts, exploits réels (reentrancy, The DAO) |
| [compilers](compilers/) | Lexing/parsing/IR/JIT, typage statique vs dynamique |
| [software-architecture](software-architecture/) | DDD, hexagonal/clean architecture, event sourcing & CQRS (complète `architecture-library`) |
| [software-engineering](software-engineering/) | Tests, revue de code, git workflows, agile |
| [resources](resources/) | Roadmaps, cheatsheets (bash/docker/git/http/k8s/linux/regex/sql), livres |

## Règle d'or

Une entrée = un fichier, un savoir réel. Pas de page vide « pour faire nombre ».
Pour ajouter une entrée aux domaines de pratique, utiliser la skill **ajouter-entree-savoir**
(ou **ajouter-recette-cookbook** pour le cookbook). Les domaines encyclopédiques se complètent au
fil de l'eau, format libre.
