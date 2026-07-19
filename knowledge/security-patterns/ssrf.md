# SSRF (Server-Side Request Forgery)

## 🎯 Menace / objectif
Empêcher qu'un attaquant force le serveur à émettre des requêtes HTTP vers des cibles qu'il ne devrait normalement jamais pouvoir atteindre lui-même — services internes non exposés publiquement, métadonnées cloud (`169.254.169.254`), ou d'autres machines du réseau interne — en exploitant une fonctionnalité applicative légitime qui fait une requête sortante basée sur une entrée utilisateur.

## 🧠 Principe
Toute fonctionnalité où le serveur récupère une ressource depuis une URL fournie (ou influencée) par l'utilisateur est un candidat SSRF potentiel : webhook de callback, aperçu d'URL, import de fichier depuis un lien, avatar chargé depuis une URL externe. L'attaquant ne cible pas le serveur directement — il l'utilise comme proxy pour atteindre des ressources internes qui font confiance à toute requête venant du réseau interne, précisément parce qu'elles ne sont pas censées être exposées à l'extérieur. Sur une infrastructure cloud, la cible classique est l'endpoint de métadonnées de l'instance (`http://169.254.169.254/latest/meta-data/`), qui peut exposer des credentials temporaires du rôle IAM de la machine sans authentification depuis l'intérieur du réseau.

## 🛠️ Mise en œuvre
La défense fiable est une allowlist stricte de destinations autorisées, pas une blocklist de ce qui est interdit — une blocklist se contourne (redirection HTTP vers une IP interne, notation IP alternative comme `0x7f000001` pour `127.0.0.1`, résolution DNS qui pointe vers une IP interne après validation initiale).

```java
@Component
class SafeUrlFetcher {
    private static final Set<String> ALLOWED_HOSTS = Set.of("api.partenaire-connu.com");

    String fetch(String requestedUrl) {
        URI uri = URI.create(requestedUrl);
        if (!ALLOWED_HOSTS.contains(uri.getHost())) {
            throw new SecurityException("Host non autorisé: " + uri.getHost());
        }
        InetAddress resolved = resolveAndValidate(uri.getHost()); // résout puis vérifie l'IP résultante
        if (resolved.isLoopbackAddress() || resolved.isSiteLocalAddress() || resolved.isLinkLocalAddress()) {
            throw new SecurityException("Adresse interne bloquée après résolution DNS");
        }
        // ... exécuter la requête avec un client HTTP configuré sans suivi automatique de redirection
        return httpClient.get(uri, /* followRedirects = */ false);
    }
}
```
Le point le plus souvent négligé : valider l'IP **après** résolution DNS, pas seulement le nom d'hôte déclaré — un attaquant peut faire pointer un domaine qu'il contrôle vers une IP interne (DNS rebinding), donc la vérification de l'hôte seule ne suffit pas si elle a lieu avant la résolution réelle utilisée pour la connexion.

## ❌ Erreurs classiques
- Bloquer seulement `localhost`/`127.0.0.1` en filtrant la chaîne de caractères, sans couvrir les autres représentations équivalentes (`0.0.0.0`, notation décimale/hexadécimale, IPv6 `::1`, ou un domaine qui résout vers ces adresses).
- Valider l'URL puis suivre les redirections HTTP automatiquement sans revalider la destination finale → la validation initiale devient inutile si le serveur cible redirige ensuite vers une IP interne.
- Traiter SSRF comme un problème uniquement lié aux URLs complètes, en oubliant les fonctionnalités qui construisent une requête sortante à partir de fragments (un `host` et un `port` séparés dans un formulaire, par exemple) — la surface d'attaque est la même, juste moins visible dans le code.
- Ne durcir que le endpoint HTTP applicatif sans restreindre aussi, en profondeur, l'accès réseau sortant du serveur lui-même (règles de firewall/security group) — la défense applicative doit se doubler d'une défense réseau, pas la remplacer.

## ✅ Vérification
Test qui fournit une URL pointant vers une adresse interne connue (`http://169.254.169.254/`, `http://127.0.0.1:8080/actuator`) à la fonctionnalité concernée et vérifie un rejet explicite avant toute tentative de connexion réseau réelle — pas seulement un test contre l'hôte de test lui-même, mais contre les représentations alternatives d'IP internes mentionnées plus haut.

## 🔗 Liens
- [xxe.md](xxe.md) — un des vecteurs qui peut mener à une SSRF via un document XML utilisateur
- [cors.md](cors.md) — mécanisme différent (protège le navigateur, pas le serveur) mais souvent confondu avec SSRF en discussion
