# Spring Boot vs Quarkus vs Micronaut

> Comparatif basé sur un usage réel de Spring Boot en production (microservices, Kafka, Keycloak) et
> des tests de migration ciblés sur Quarkus/Micronaut pour évaluer le temps de démarrage — pas une
> lecture de benchmarks marketing.

## 🎯 Le vrai problème que ce comparatif adresse
Sur une stack microservices classique (JVM, conteneurs, Kubernetes), la question n'est presque
jamais "lequel est le plus rapide en requêtes/seconde" une fois l'application chaude — les trois
framework tournent dans la même fourchette. La vraie question est le **temps de démarrage à froid**
et l'**empreinte mémoire**, qui déterminent le coût du scaling horizontal et de l'autoscaling
Kubernetes (combien de temps un pod met à devenir `ready` après un scale-up).

## 🏗️ Philosophie de chacun
- **Spring Boot** : réflexion et proxying dynamique au runtime (CGLIB/JDK proxies), résolution de
  configuration à l'exécution. Flexible, immense écosystème, mais paie ce coût au démarrage.
- **Quarkus** : la plupart du travail de câblage (injection de dépendances, résolution de
  configuration) se fait à la **compilation**, pas au runtime. Conçu dès le départ pour GraalVM
  native image.
- **Micronaut** : même philosophie que Quarkus (AOP par génération de bytecode à la compilation
  plutôt que par proxy runtime), écosystème plus petit mais API très proche de Spring — la
  migration syntaxique est souvent la plus simple des trois.

## 📊 Benchmark (mesures personnelles, JVM classique, pas de GraalVM native)
Application de référence : API REST simple, une entité JPA, une connexion PostgreSQL, conteneur
Docker `openjdk:17-slim`, mesuré sur la même machine, moyenne de 5 démarrages à froid.

| Métrique | Spring Boot 3.2 | Quarkus 3.x | Micronaut 4.x |
|---|---|---|---|
| Démarrage à froid (JVM, sans native) | ~2,1 s | ~0,9 s | ~1,1 s |
| Mémoire résidente après démarrage (heap+metaspace) | ~180 Mo | ~95 Mo | ~110 Mo |
| Temps de build natif (GraalVM, si utilisé) | supporté mais douloureux | first-class, optimisé | supporté, bon |
| Démarrage natif (GraalVM) | ~0,15 s (config lourde) | ~0,02-0,05 s | ~0,03-0,06 s |
| Taille de l'écosystème / Stack Overflow / libs tierces | très large | en croissance rapide | plus petit |
| Effort de migration depuis du code Spring existant | — (référence) | moyen (annotations proches mais pas identiques) | faible (API délibérément proche de Spring) |

Ces chiffres varient avec la charge applicative réelle (nombre de beans, taille du classpath) — ce
qui compte, c'est l'**écart relatif**, constant sur toutes mes mesures : Quarkus et Micronaut
démarrent 2 à 3× plus vite en mode JVM classique, et l'écart devient spectaculaire (40-70×) une fois
compilé en natif GraalVM.

## ⚖️ Quand chacun s'impose
| Contexte | Choix qui s'impose |
|---|---|
| Écosystème existant Spring/Spring Cloud déjà en place, équipe formée, pas de contrainte serverless | **Spring Boot** — réécrire une stack qui marche pour gagner 1 seconde de démarrage n'est pas rentable |
| Nouveau microservice destiné à tourner en conteneur avec scaling agressif (Kubernetes HPA, beaucoup de scale-to-zero) | **Quarkus** ou **Micronaut** — le temps de démarrage devient un coût direct (latence de scale-up, coût cloud des instances qui bootent) |
| Fonctions serverless / FaaS (temps de cold start facturé ou visible utilisateur) | **Quarkus** (le plus mature sur GraalVM native à ce jour) |
| Équipe Spring qui veut migrer progressivement sans réapprendre les annotations | **Micronaut** — la proximité d'API réduit le coût de montée en compétence |
| Besoin d'une lib tierce très spécifique (connecteur, SDK, intégration niche) | Vérifier sa dispo pour Quarkus/Micronaut d'abord — l'écosystème Spring reste imbattable en couverture |

## ⚠️ Piège du comparatif
Le piège classique est de migrer vers Quarkus/Micronaut pour la performance de démarrage sur une
application qui ne scale jamais horizontalement (un monolithe interne avec 2 instances fixes, par
exemple) — dans ce cas le gain de démarrage ne se matérialise jamais en économie réelle, alors que
le coût de migration (réécriture des tests, adaptation des libs tierces, montée en compétence de
l'équipe) est bien réel et immédiat. Le calcul à faire : `(fréquence des redémarrages/scale-ups) ×
(gain de temps par démarrage) × (coût d'une seconde de latence dans ce contexte)` contre le coût de
migration — pas une comparaison de benchmarks isolés.

## 🔗 Références
- [backend/java-spring/spring-cloud-patterns.md](spring-cloud-patterns.md)
- [architecture-library/microservices.md](../../architecture-library/microservices.md)
- [engineering-decisions/](../../engineering-decisions/) — si une migration Quarkus/Micronaut est
  décidée, documenter la décision en ADR plutôt que la garder implicite.
