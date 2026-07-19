# CORS (Cross-Origin Resource Sharing)

## 🎯 Menace / objectif
Empêcher qu'un site web arbitraire, ouvert dans le navigateur d'un utilisateur déjà authentifié
ailleurs, puisse appeler une API et lire la réponse en son nom — sans bloquer les appels
légitimes venant du frontend réel de l'application.

## 🧠 Principe
Le navigateur applique nativement la **same-origin policy** : par défaut, du JavaScript exécuté
sur `evil.com` ne peut pas lire la réponse d'un appel `fetch()` vers `api.notesapp.com`. CORS est
le mécanisme qui **assouplit** cette restriction, de façon contrôlée : le serveur déclare
explicitement quelles origines ont le droit de lire ses réponses via des en-têtes de réponse
(`Access-Control-Allow-Origin`). Point clé souvent mal compris : **CORS ne bloque rien côté
serveur** — la requête part quand même, c'est le navigateur qui empêche le script appelant de
lire la réponse si l'origine n'est pas autorisée. Un client qui n'est pas un navigateur (curl,
un autre backend) ignore totalement CORS.

## 🛠️ Mise en œuvre
```java
@Configuration
class CorsConfig {
    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("https://app.notesapp.com")   // jamais "*" avec credentials
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("Authorization", "Content-Type")
                        .allowCredentials(true)
                        .maxAge(3600);  // duree de cache du preflight cote navigateur
            }
        };
    }
}
```
Pour une requête "non simple" (méthode autre que GET/POST simple, header custom comme
`Authorization`), le navigateur envoie d'abord une requête **preflight** (`OPTIONS`) pour vérifier
que le serveur autorise l'appel réel avant de l'envoyer — Spring gère ça automatiquement une fois
la config CORS en place.

## ❌ Erreurs classiques
- `allowedOrigins("*")` combiné à `allowCredentials(true)` — combinaison explicitement interdite
  par la spec CORS (et rejetée par les navigateurs modernes), parce que ça reviendrait à autoriser
  n'importe quel site à faire des appels authentifiés au nom de l'utilisateur.
- Configurer CORS en pensant que ça protège l'API — **ça ne protège rien côté serveur**, un
  attaquant qui appelle l'API directement (pas depuis un navigateur) n'est jamais concerné par
  CORS. L'authentification/autorisation reste l'unique vraie protection ; CORS protège
  l'utilisateur final contre un site tiers malveillant, pas l'API contre un attaquant direct.
- Whitelist d'origines trop large "pour aller plus vite en dev" qui survit jusqu'en prod
  (`allowedOrigins("*")` ou une regex trop permissive) — à revoir explicitement avant chaque
  déploiement, pas seulement à la mise en place initiale.

## ✅ Vérification
Depuis la console du navigateur, sur un domaine non whitelisté, tenter un `fetch()` vers l'API et
vérifier que la réponse est bloquée côté navigateur (erreur CORS visible dans la console, pas une
erreur HTTP côté réseau — la requête part bien, seule la lecture de la réponse est bloquée).
Vérifier ensuite que le même appel depuis l'origine autorisée fonctionne normalement, preflight
inclus (visible dans l'onglet Réseau : une requête `OPTIONS` suivie de la requête réelle).

## 🔗 Liens
- [oauth2-keycloak.md](oauth2-keycloak.md) — l'authentification qui protège réellement l'API,
  indépendamment de la config CORS
