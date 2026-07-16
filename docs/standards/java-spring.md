# Standard — Java / Spring

Stack de référence : **Java 17**, **Spring Boot 3.2.5**, Spring Cloud, Maven. `groupId com.jihedapps`.

## Structure
- Découpage par couches : `controller` → `service` → `repository`, DTO en entrée/sortie (jamais l'entité JPA exposée directement).
- Un module = un `pom.xml` héritant de `spring-boot-starter-parent`.
- Package racine `com.jihedapps.<projet>`.

## Règles
- **Java 17** : records pour les DTO, `switch` expressions, pas de Lombok sur les DTO (records suffisent).
- **Validation** aux frontières : `@Valid` + contraintes Jakarta sur les DTO d'entrée.
- **Pas d'entité lazy itérée en boucle** → `JOIN FETCH` / `@EntityGraph` (cf. failures N+1).
- **Transactions** : `@Transactional` au niveau service, jamais controller ni repository.
- **Exceptions** : `@ControllerAdvice` central, réponse d'erreur homogène (voir api-design-guide).
- **Config** : jamais de valeur d'infra en dur → `application.properties` + variables d'env (profils `dev`/`docker`).

## Commandes
```bash
mvn clean verify          # build + tests
mvn spring-boot:run       # run local
mvn -pl backend test      # tests d'un module
```

## Ce qu'on évite
- `field injection` (`@Autowired` sur champ) → injection par constructeur.
- Logique métier dans le controller.
- Renvoyer des entités Hibernate directement (fuite de lazy + couplage schéma/API).
