# knowledge/ — La bibliothèque de savoir

Les 80 % de l'écosystème. Ici on réfléchit plus qu'on ne code. Chaque dossier est un domaine ;
chaque fichier est **une** entrée autonome. Les domaines "maison" (nés dans ce dépôt) suivent le
`_TEMPLATE.md` du domaine quand il existe. Les domaines absorbés depuis `dev-library` gardent leur
format libre d'origine — les reformater juste pour l'uniformité n'aurait rien ajouté au contenu.

## Domaines de pratique

Ce qui existait déjà ici, lié à l'expérience directe et aux projets du dépôt.

| Domaine | Ce qu'on y met | Entrées |
|---|---|---|
| [architecture-library](architecture-library/) | Styles d'architecture : pourquoi, quand (pas), trade-offs | 14 patterns |
| [engineering-failures](engineering-failures/) | Pannes vécues : cause, symptômes, diagnostic, prévention | 15 cas |
| [debugging-recipes](debugging-recipes/) | « Ça ne marche pas » → checklist de diagnostic ciblée | 9 recettes |
| [security-patterns](security-patterns/) | JWT, OAuth2, Keycloak, RBAC, OWASP | 12 patterns |
| [devsecops](devsecops/) | CI/CD, conteneurs, IaC, monitoring, sécurité pipeline | 12 fiches |
| [backend](backend/) | Java/Spring, Node.js, APIs, microservices | 9 fiches |
| [database-engineering](database-engineering/) | Par moteur : Postgres, Oracle, Redis, Mongo, Neo4j, Elasticsearch | 7 moteurs |
| [telecom](telecom/) | BSS/OSS, 5G, provisioning, number portability, billing — ancré dans dix ans de terrain | 26 fiches |

## Domaines absorbés depuis dev-library (consolidation du 23 juillet 2026)

`dev-library` avait fini par recouvrir les mêmes sujets que ce dépôt sans jamais devenir plus
qu'une seconde bibliothèque à moitié à jour. Le contenu qui n'existait nulle part ici est arrivé
tel quel, historique Git compris (voir [`docs/governance/migration-map.md`](../docs/governance/migration-map.md)
pour le détail des décisions). Ce qui faisait doublon (backend, devsecops, patterns d'architecture,
telecom) n'a pas été recopié — les versions déjà en place ici étaient au moins aussi complètes.

| Domaine | Ce qu'on y trouve | Entrées |
|---|---|---|
| [ai-engineering](ai-engineering/) | RAG, agents, inférence locale, évaluation, hooks/skills, guardrails, routing/coût | 41 fiches |
| [cybersecurity](cybersecurity/) | Fondamentaux offense/défense, pentest, blue team, parcours d'apprentissage | 5 fiches |
| [code-review](code-review/) | Comment reviewer et être reviewé, des deux côtés | 1 fiche |
| [practices](practices/) | Tests, git workflows, agile — le liant entre les domaines techniques | 3 fiches |

Le contenu généraliste (langages, web, cloud, front, mobile, etc., sans lien direct avec la
pratique d'ingénierie de ce dépôt) vit séparément dans [`foundations/`](../foundations/README.md).

## Règle d'or

Une entrée = un fichier, un savoir réel. Pas de page vide « pour faire nombre ». Si un sujet a
déjà une entrée quelque part dans `knowledge/` ou `foundations/`, on l'étend plutôt que d'en ouvrir
une seconde — le glossaire ([`GLOSSARY.md`](GLOSSARY.md)) est là pour repérer les recoupements
avant d'écrire.
