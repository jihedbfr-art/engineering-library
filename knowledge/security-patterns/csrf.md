# CSRF (Cross-Site Request Forgery)

## 🎯 Menace / objectif
Empêcher qu'un site malveillant fasse exécuter, à l'insu de l'utilisateur, une action authentifiée
sur une autre application où il est déjà connecté — typiquement en s'appuyant sur un cookie de
session que le navigateur envoie automatiquement à chaque requête vers ce domaine, peu importe
d'où part la requête.

## 🧠 Principe
Le navigateur attache automatiquement les cookies d'un domaine à toute requête vers ce domaine,
même déclenchée depuis une page tierce (une balise `<img>`, un formulaire auto-soumis, un
`fetch()` avec `credentials: include`). Si l'application ne fait que vérifier "y a-t-il un cookie
de session valide", elle ne peut pas distinguer une requête légitime venant de son propre frontend
d'une requête forgée depuis `evil.com`. La défense consiste à exiger une preuve supplémentaire que
seul le vrai frontend peut fournir — un jeton CSRF que le site malveillant ne peut pas connaître ni
deviner.

## 🛠️ Mise en œuvre
**Important en préambule** : une API stateless qui utilise un JWT en header `Authorization`
(pas de cookie de session) n'est **pas** vulnérable au CSRF au sens classique — un site tiers ne
peut pas forcer le navigateur à ajouter un header `Authorization` à sa place. CSRF concerne
spécifiquement les architectures à cookie de session ; voir
[oauth2-keycloak.md](oauth2-keycloak.md) pour le modèle stateless qui évite le problème par
construction plutôt que de le mitiger.

Pour une application qui utilise malgré tout un cookie de session (SSR classique, Thymeleaf) :
```java
@Configuration
class SecurityConfig {
    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        return http.build();
    }
}
```
```html
<!-- Thymeleaf injecte automatiquement le token CSRF dans chaque formulaire -->
<form method="post" th:action="@{/tickets/42/status}">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
</form>
```
Le principe : le token est stocké côté client (cookie) mais doit être renvoyé explicitement dans
le corps ou un header de la requête — un site tiers peut faire envoyer le cookie automatiquement,
mais ne peut pas lire sa valeur (same-origin policy) pour le recopier dans le champ attendu.

## ❌ Erreurs classiques
- Désactiver CSRF globalement (`http.csrf(csrf -> csrf.disable())`) parce que "l'API est REST"
  sans vérifier que l'authentification est réellement stateless (JWT en header) — si l'app garde
  un cookie de session en parallèle pour une autre raison, désactiver CSRF sans y penser rouvre la
  faille.
- Confondre CORS et CSRF : CORS protège contre la **lecture** d'une réponse par un site tiers,
  CSRF protège contre l'**exécution** d'une action — les deux mécanismes sont complémentaires,
  voir [cors.md](cors.md), aucun des deux ne remplace l'autre.
- Mettre le token CSRF dans un cookie **sans** exiger qu'il soit aussi renvoyé ailleurs (header ou
  body) — un cookie seul ne prouve rien puisque c'est justement ce que le navigateur envoie
  automatiquement, y compris depuis une requête forgée.

## ✅ Vérification
Depuis une page HTML externe (pas l'application), tenter de soumettre un formulaire vers
l'endpoint protégé en s'appuyant uniquement sur le cookie de session existant (sans passer par le
frontend légitime) et vérifier que la requête est rejetée faute de token CSRF valide.

## 🔗 Liens
- [cors.md](cors.md) — mécanisme complémentaire, protège la lecture plutôt que l'exécution
- [oauth2-keycloak.md](oauth2-keycloak.md) — le modèle stateless (JWT en header) qui rend CSRF
  non applicable par construction
