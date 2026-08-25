# Engineering Library

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
![Domaines de connaissance](https://img.shields.io/badge/knowledge%20domains-12-informational)
![Stack](https://img.shields.io/badge/stack-Java%20%2F%20Spring%20%2F%20Angular-brightgreen)
[![Build](https://github.com/jihedbfr-art/engineering-library/actions/workflows/build.yml/badge.svg)](https://github.com/jihedbfr-art/engineering-library/actions/workflows/build.yml)
[![CodeQL](https://github.com/jihedbfr-art/engineering-library/actions/workflows/codeql.yml/badge.svg)](https://github.com/jihedbfr-art/engineering-library/actions/workflows/codeql.yml)

[English version](./README.md)

Des notes d'ingénierie Java/Spring construites sur plus de 10 ans passés dans le BSS télécom et le
provisioning réseau, avant de basculer vers l'architecture microservices. Ce n'est pas une
encyclopédie généraliste du développement — ça a essayé de l'être, et c'était pire pour ça. Ce qui
reste, c'est ce que je peux réellement défendre dans une conversation : des décisions
d'architecture, des échecs, des recettes de debug, et un guide du domaine télécom construit à
partir de choses comme la portabilité de numéro et une migration de cœur de réseau Nokia vers
Huawei, pas en lisant Wikipédia.

Je faisais tourner un second dépôt, `dev-library`, pour tout ce qui était plus généraliste —
langages, fondamentaux web, cloud, IA. Il a fini par devenir une copie parallèle des mêmes sujets
déjà couverts ici, en moins bien maintenu. Je l'ai fusionné dans celui-ci en juillet 2026 (avec son
historique Git intact — voir [`docs/governance/migration-map.md`](docs/governance/migration-map.md)
pour le raisonnement complet, en anglais) plutôt que de laisser vivre deux bibliothèques à moitié
finies.

## Ce qu'il y a ici

Si vous voulez comprendre comment quelque chose fonctionne en général, commencez dans
`foundations/`. Si vous voulez savoir comment le construire, le faire tourner ou le corriger avec
cette stack précise, c'est dans `knowledge/`. Le code exécutable vit dans `projects/`, gradué d'une
CLI de dix lignes jusqu'à une plateforme multi-services.

```
engineering-library/
├── projects/                  nano → micro → mini → standard → macro : projets Java/Spring/Angular
├── knowledge/                  choisir, construire, exploiter, déboguer — cette stack, en production
│   ├── telecom/                BSS/OSS, provisioning, cœur 5G, portabilité de numéro, roaming
│   ├── backend/                Java/Spring, Node.js, microservices, APIs
│   ├── architecture-library/   patterns d'architecture et ADR
│   ├── engineering-failures/   post-mortems et pannes, écrits honnêtement
│   ├── debugging-recipes/      comment j'ai réellement traqué des bugs précis
│   ├── database-engineering/   Postgres/Oracle, conception de schémas et de requêtes
│   ├── security-patterns/      auth, SSO, patterns de sécurité API
│   ├── devsecops/              CI/CD, sécurité des pipelines
│   ├── ai-engineering/         RAG, agents, évaluations, hooks/skills, routage de modèles et coût
│   ├── cybersecurity/          fondamentaux offense/défense, méthodologie de pentest, blue team
│   ├── code-review/            relire du code et être relu
│   └── practices/              tests, workflows Git, agilité
├── foundations/                 langages, web, cloud, mobile, et autres bases généralistes
└── docs/standards/             conventions Java/Spring, Angular, SQL, sécurité que j'impose aux projets
```

Chaque dossier de `knowledge/` a son propre README, et ceux qui existaient dès le début portent
aussi un `_TEMPLATE.md` pour que les nouvelles entrées restent cohérentes. Les entrées sont
ajoutées au fil de ce que je rencontre réellement, pas par lots — certains dossiers sont plus
minces que d'autres, et c'est honnête, pas un oubli.

## Pourquoi le télécom est la partie intéressante

La plupart des contenus Java/Spring en ligne viennent de gens qui n'ont travaillé que sur des
backends web/e-commerce. `knowledge/telecom/` est l'inverse : la portabilité de numéro modélisée
comme une machine à états multi-opérateurs, une vraie migration de cœur 5G, une architecture de
provisioning qui doit survivre à la panne d'un réseau partenaire. C'est la seule section de ce
dépôt que personne d'autre ne peut vraiment écrire.

## Licence

MIT — voir [LICENSE](LICENSE).
