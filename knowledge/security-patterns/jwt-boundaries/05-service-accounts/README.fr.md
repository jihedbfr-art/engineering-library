# Identités de comptes de service

[English version](README.md) · [Parcours](../README.fr.md)

Le realm possède quatre clients confidentiels avec comptes de service. Les tests demandent des jetons avec `client_credentials` ; aucun grant de mot de passe utilisateur. Lecteur et administrateur portent des rôles différents. Le client sans rôle possède la bonne audience mais échoue à l’autorisation ; celui d’une autre audience échoue à l’authentification malgré le rôle lecteur configuré.

Scope mappings et affectations des rôles aux comptes de service sont explicites. `fullScopeAllowed` reste faux. Le realm utilise un mapper de rôles clients et un mapper d’audience dédiés, sans paquet de scopes par défaut qui masquerait le cas négatif d’audience.

Ces identifiants de fixture sont des valeurs de démonstration publiques, réservées au realm local. Le grant authentifie un service, pas un humain ni une session déléguée. Connexion navigateur, PKCE et token exchange exigent d’autres flux et tests. Renommer un compte de service ne prouve pas qu’un humain a approuvé une action.

## Rejouer

Depuis le clone du laboratoire à la révision fixée dans le parcours, après installation locale du toolkit :

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=KeycloakServiceAccountTest' test
```

## Exercices corrigés

1. client_credentials prouve-t-il la connexion d’un humain ?

<details>
<summary>Correction</summary>

Non. Il authentifie le client/compte de service configuré. Le laboratoire ne possède pas de flux de délégation humaine.

</details>

2. Quels clients Keycloak distinguent audience et rôle ?

<details>
<summary>Correction</summary>

lab-other-audience porte la configuration lecteur mais reçoit 401 ; lab-no-role possède la bonne audience mais reçoit 403.

</details>

3. Pourquoi garder des scope mappings explicites ?

<details>
<summary>Correction</summary>

Ils rendent les rôles autorisés visibles à côté des affectations du compte de service ; le fixture ne dépend pas de Full Scope Allowed.

</details>

## Sources et preuve

- [Tests de cette révision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/KeycloakServiceAccountTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- [Keycloak](https://www.keycloak.org/docs/latest/server_admin/index.html#_service_accounts).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
