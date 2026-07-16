# `401 Unauthorized` / `invalid_token` malgré un token qui semble valide

> L'utilisateur est bien connecté côté Keycloak, mais l'appel à l'API backend échoue en 401 avec
> un token qui, décodé, semble pourtant correct.

## Causes probables (fréquentes → rares)
1. L'`issuer-uri` configuré côté resource server Spring ne correspond pas exactement à celui du
   token émis (différence d'URL — souvent `localhost` côté token vs nom de service Docker côté
   validation, ou inversement).
2. Le token est expiré au moment de l'appel (durée de vie courte, horloge désynchronisée entre
   services).
3. Le realm ou le client Keycloak utilisé pour émettre le token ne correspond pas à celui attendu
   par le resource server (plusieurs realms/clients dans l'environnement de test).

## Diagnostic pas-à-pas
```text
# 1. Décoder le token (jwt.io ou équivalent) et comparer le claim "iss" à la config Spring
spring.security.oauth2.resourceserver.jwt.issuer-uri
# 2. Vérifier la date d'expiration du token (claim "exp") contre l'heure système du serveur
# 3. Vérifier que le realm/client dans le claim correspond au realm configuré côté resource server
```

## Correctif
- Aligner exactement l'`issuer-uri` configuré avec celui réellement utilisé par le client pour
  obtenir son token (attention aux environnements Docker où le nom d'hôte diffère entre le
  navigateur et le réseau interne des conteneurs).
- Synchroniser les horloges des services (NTP) si l'écart d'expiration provient d'une dérive
  d'horloge entre conteneurs.

## Si ça ne suffit pas
Vérifier aussi les rôles/scopes attendus par l'endpoint (`403 Forbidden` plutôt que `401` dans ce
cas) — un token valide sur un realm correct peut quand même manquer le rôle nécessaire pour
l'action demandée.
