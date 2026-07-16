# JWT vs Session

## 🎯 Principe
Le choix entre token JWT stateless et session stateful côté serveur détermine où vit l'état
d'authentification — jamais un choix neutre, il a des conséquences directes sur la scalabilité et
la révocation.

## ✅ Bonne pratique — JWT (stateless)
Adapté aux architectures microservices où plusieurs services doivent vérifier l'identité sans
appeler un service central à chaque requête (cf. la sécurisation OAuth2/OIDC via Keycloak, cf.
[security-patterns/oauth2-keycloak.md](../security-patterns/oauth2-keycloak.md)) :
```http
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```
Chaque service valide la signature localement (clé publique de l'IAM), sans appel réseau
supplémentaire.

## ❌ Contre-exemple — Session classique dans ce contexte
```http
Cookie: JSESSIONID=A1B2C3...
```
Nécessite une session partagée (sticky session ou store de session centralisé type Redis) entre
toutes les instances d'un service derrière un load balancer — ajoute une dépendance et un point de
synchronisation que le JWT stateless évite par nature.

## 💡 Exemple concret
Sur une architecture microservices avec API Gateway (Spring Cloud Gateway, Eureka), le JWT permet à
chaque service en aval de valider l'identité indépendamment sans dépendre d'un store de session
partagé — c'est le choix par défaut pour ce type d'architecture. La contrepartie : un JWT ne peut
pas être révoqué instantanément avant son expiration (contrairement à une session qu'on invalide
immédiatement côté serveur) — pour un besoin de révocation immédiate (déconnexion forcée,
compromission de compte), prévoir une liste de révocation courte ou des tokens à durée de vie
volontairement courte avec refresh token.
