# knowledge/ — La bibliothèque de savoir

Les 80 % de l'écosystème. Ici on réfléchit plus qu'on ne code. Chaque dossier est un domaine ;
chaque fichier est **une** entrée autonome qui suit le `_TEMPLATE.md` de son domaine.

## Domaines

| Domaine | Ce qu'on y met | État |
|---|---|---|
| [architecture-library](architecture-library/) | Styles d'architecture : pourquoi, quand (pas), trade-offs, diagrammes | squelette + 1 exemple |
| [engineering-decisions](engineering-decisions/) | ADR numérotés : « Pourquoi X ? » avec contexte, options, conséquences | squelette + exemples |
| [engineering-failures](engineering-failures/) | Pannes vécues : cause, symptômes, diagnostic, solution, prévention | squelette + 1 exemple |
| [debugging-recipes](debugging-recipes/) | « Ça ne marche pas » → checklist de diagnostic ciblée | squelette |
| [performance-recipes](performance-recipes/) | Optimisations SQL / JVM / GC / Hibernate / cache / Kafka | squelette |
| [security-patterns](security-patterns/) | JWT, OAuth2, Keycloak, RBAC, OWASP Top 10 | squelette + 1 exemple |
| [api-design-guide](api-design-guide/) | REST, GraphQL, gRPC, versioning, pagination, idempotence | squelette |
| [database-engineering](database-engineering/) | Par moteur : index, transactions, isolation, MVCC, réplication | squelette |
| [ai-engineering](ai-engineering/) | Hooks, prompts, agents, MCP, RAG, évaluation, context engineering | squelette |
| [engineering-playbooks](engineering-playbooks/) | Procédures « 2h du matin » : incident, rollback, migration | squelette |
| [engineering-checklists](engineering-checklists/) | Avant merge / prod / migration / release | squelette + 1 exemple |
| [code-review-guide](code-review-guide/) | Comment reviewer : smells, anti-patterns, perf, tests, sécurité | squelette |
| [engineering-cookbook](engineering-cookbook/) | « Je veux faire X » → recette prête à l'emploi | squelette + 1 exemple |

## Règle d'or

Une entrée = un fichier, un template, un savoir réel. Pas de page vide « pour faire nombre ».
Pour ajouter une entrée, utiliser la skill **ajouter-entree-savoir** (ou **ajouter-recette-cookbook** pour le cookbook).
