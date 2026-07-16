# OAuth2 + Keycloak (Spring Resource Server)

## 🎯 Menace / objectif
Sécuriser des API microservices sans partager de secret ni gérer soi-même les mots de passe : chaque
service valide un token émis par un serveur d'identité central (Keycloak).

## 🧠 Principe
Keycloak (Authorization Server OIDC) émet des JWT signés. Le backend est **Resource Server** : il ne
crée pas de session, il **valide** le JWT à chaque requête via la clé publique exposée par Keycloak
(endpoint JWKS). Stateless → scalable horizontalement.

## 🛠️ Mise en œuvre
Dépendance : `spring-boot-starter-oauth2-resource-server` (déjà présente dans le backend).

```properties
# application.properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://keycloak:8080/realms/notesapp
```
```java
@Configuration
@EnableWebSecurity
class SecurityConfig {
  @Bean SecurityFilterChain filter(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(a -> a.anyRequest().authenticated())
        .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
    return http.build();
  }
}
```

## ❌ Erreurs classiques
- Valider seulement la signature mais **pas** `iss`/`aud` → un token d'un autre realm passe.
- Mettre `issuer-uri` en `localhost` alors que dans Docker c'est le nom de service `keycloak`.
- Mapper les rôles Keycloak au mauvais endroit : ils sont sous `realm_access.roles`, il faut un
  `JwtAuthenticationConverter` pour les exposer en `GrantedAuthority`.

## ✅ Vérification
Appeler un endpoint protégé sans token → `401` ; avec un token expiré → `401` ; avec un token valide
mais rôle manquant → `403`.

## 🔗 Liens
- [engineering-decisions/0001-pourquoi-keycloak.md](../engineering-decisions/0001-pourquoi-keycloak.md)
- [engineering-cookbook/jwt-resource-server-spring.md](../engineering-cookbook/jwt-resource-server-spring.md)
