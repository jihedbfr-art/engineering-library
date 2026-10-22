# L’audience est une frontière distincte

[English version](README.md) · [Parcours](../README.fr.md)

L’exemple accepte l’audience `toolkit-lab`. L’issuer indique l’émetteur du jeton ; l’audience, son destinataire. Ni un rôle ni un nom de client ne remplacent ce contrôle.

La première implémentation du laboratoire configurait issuer et dates sans validateur d’audience. Deux tests signés ont alors observé 200 avec une audience absente ou différente. L’ajout d’un validateur composé a changé ces deux résultats en 401 tout en conservant le succès du lecteur. C’est une leçon de configuration applicative : le convertisseur de rôles ne fournit pas cette politique à sa place.

Le fixture Keycloak sépare le mapper d’audience du mapper de rôles. `lab-other-audience` possède encore le rôle lecteur, mais cible `other-api` ; sa requête HTTP est refusée. Conserver ce client négatif lors d’une modification du realm. Le retirer faciliterait la confusion entre mapping de rôles réussi et validation complète du jeton.

## Rejouer

Depuis le clone du laboratoire à la révision fixée dans le parcours, après installation locale du toolkit :

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=JwtBoundaryTest#wrongAudienceIsRejectedDespiteReaderRole+missingAudienceIsRejected+validReaderIsAccepted' test
```

## Exercices corrigés

1. Un jeton lecteur correctement signé cible une autre API. Quel résultat exige ce laboratoire ?

<details>
<summary>Correction</summary>

401, avant que le rôle lecteur puisse autoriser cette ressource.

</details>

2. Une audience absente peut-elle devenir une correspondance implicite ?

<details>
<summary>Correction</summary>

Non. L’exemple la refuse ; son test négatif doit échouer si ce contrôle est retiré.

</details>

3. Où sont configurés politique d’audience et conversion des rôles ?

<details>
<summary>Correction</summary>

L’application consommatrice compose les validateurs JWT. Le toolkit convertit ensuite les rôles du client choisi.

</details>

## Sources et preuve

- [Tests de cette révision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- [Keycloak](https://www.keycloak.org/docs/latest/server_admin/index.html#_service_accounts).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
