# RBAC (Role-Based Access Control)

## 🎯 Menace / objectif
Empêcher qu'un utilisateur accède à une action ou une ressource pour laquelle il n'a pas le rôle requis — la forme la plus courante de contrôle d'accès, et la base sur laquelle beaucoup d'incidents de sécurité applicative se jouent quand elle est mal implémentée (vérification côté frontend seulement, rôle vérifié à la création du token mais jamais revalidé).

## 🧠 Principe
On n'attribue jamais une permission directement à un utilisateur — on l'attribue à un rôle, et on attribue des rôles aux utilisateurs. `ROLE_ADMIN` peut supprimer un ticket, `ROLE_USER` ne peut que le créer et le consulter. L'indirection par le rôle est ce qui rend le modèle gérable à l'échelle : changer les permissions d'un rôle change instantanément les droits de tous les utilisateurs qui le portent, sans toucher à chaque compte individuellement.

## 🛠️ Mise en œuvre
Avec Keycloak comme fournisseur d'identité, les rôles vivent dans le token JWT (`realm_access.roles` pour les rôles globaux, `resource_access.<client>.roles` pour les rôles spécifiques à un client) — voir [oauth2-keycloak.md](oauth2-keycloak.md) pour la configuration côté resource server qui extrait ces rôles en `GrantedAuthority` Spring Security.

```java
@Configuration
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.DELETE, "/tickets/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/tickets/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated());
        return http.build();
    }
}

@Service
class TicketService {
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long ticketId) { /* ... */ }
}
```
Deux niveaux de vérification qui se complètent, pas qui se remplacent : au niveau de la chaîne de filtres HTTP pour les patterns d'URL simples, et `@PreAuthorize` au niveau méthode pour une granularité plus fine (ex: un `ADMIN` peut supprimer, mais pas forcément modifier un champ précis).

## ❌ Erreurs classiques
- Vérifier le rôle uniquement côté frontend (masquer un bouton "Supprimer" pour un non-admin) sans revérifier côté backend → n'importe qui peut appeler directement l'endpoint avec un client HTTP, le frontend n'est jamais une frontière de sécurité.
- Coder des vérifications de rôle en dur dans la logique métier (`if (user.getRole().equals("ADMIN"))`) plutôt que de s'appuyer sur le mécanisme déclaratif de Spring Security → devient impossible à auditer globalement, chaque contrôle vit isolé dans son coin du code.
- Confondre authentification et autorisation : un token JWT valide prouve qui est l'utilisateur, pas ce qu'il a le droit de faire — un endpoint qui vérifie seulement "le token est valide" sans vérifier le rôle est authentifié mais pas autorisé.
- Rôles trop granulaires (`ROLE_CAN_EDIT_TICKET_TITLE_ONLY`) qui explosent en combinatoire ingérable → au-delà d'un certain niveau de granularité par ressource individuelle, voir [abac.md](abac.md), RBAC n'est plus le bon outil.

## ✅ Vérification
Test d'intégration qui appelle chaque endpoint sensible avec un token portant un rôle insuffisant et vérifie un `403 Forbidden` (pas un `401`, qui signifierait un problème d'authentification et non d'autorisation) :
```java
mockMvc.perform(delete("/tickets/1").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
       .andExpect(status().isForbidden());
```

## 🔗 Liens
- [oauth2-keycloak.md](oauth2-keycloak.md) — comment les rôles arrivent dans le token JWT
- [abac.md](abac.md) — quand RBAC devient trop rigide (permission qui dépend du contexte, pas juste du rôle)
