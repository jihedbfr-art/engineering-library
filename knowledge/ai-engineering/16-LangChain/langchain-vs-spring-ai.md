# LangChain vs Spring AI — pour un backend Java/Spring

## 🎯 Objectif
Décider s'il faut passer par LangChain (Python/JS) ou rester sur Spring AI pour intégrer un LLM
dans une architecture déjà backend Java/Spring, plutôt que d'adopter LangChain par réflexe parce
que c'est l'écosystème le plus cité.

## 🧩 Contexte d'usage
Une équipe backend Java doit ajouter une fonctionnalité IA (RAG, agent, tool calling) à un système
existant en Spring Boot/microservices.

## 🛠️ Recette
Grille de décision :

| Critère | Spring AI | LangChain |
|---|---|---|
| Stack existante | Déjà 100% Java/Spring (JHipster, Spring Cloud) | Déjà Python/Node dans l'équipe |
| Intégration avec le domaine métier | Directe (`@Tool` sur les `@Service` existants, cf. `15-Spring-AI`) | Nécessite un pont/service séparé si le métier est en Java |
| Observabilité/CI | Réutilise la stack Spring existante (Micrometer, Actuator) | Stack Python séparée à opérer en plus |
| Écosystème d'intégrations tierces | Plus restreint mais suffisant pour PGVector/Kafka/Keycloak | Très large (utile si beaucoup de connecteurs exotiques) |
| Équipe qui maintient | Devs Spring existants, pas de nouvelle stack | Nécessite une compétence Python dédiée |

Pour une équipe déjà organisée autour de Spring Boot/Spring Cloud (comme le stack de ce dépôt),
Spring AI évite d'introduire un second runtime (Python) juste pour la partie IA, et garde
l'observabilité/la sécurité dans le même système que le reste des microservices.

## ✅ Résultat attendu
Une fonctionnalité IA intégrée sans dupliquer d'infrastructure (déploiement, monitoring, sécurité)
pour un seul composant du système — sauf si le besoin réel justifie un service Python séparé
(équipe data science existante, modèles ML custom non-LLM à héberger).

## ⚠️ Piège
- **Choisir LangChain uniquement par notoriété** : la richesse de son écosystème ne compense pas le
  coût d'opérer un second runtime si l'équipe et l'infrastructure sont 100% Java.
- **Sous-estimer Spring AI par méconnaissance** : Spring AI couvre déjà RAG, tool calling, MCP et
  observabilité (cf. les autres fiches de ce domaine) — il n'est pas "en retard" sur les besoins
  courants d'une application d'entreprise.
