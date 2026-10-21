# Rôles clients et mapping des autorités

[English version](README.md) · [Parcours](../README.fr.md)

Ce laboratoire extrait les rôles de `resource_access.toolkit-lab.roles`. Le mapping des rôles de realm est désactivé. La chaîne de filtres utilise explicitement le convertisseur du toolkit ; les règles des endpoints demandent ensuite `ROLE_READER` ou `ROLE_ADMIN`.

`roleForAnotherClientDoesNotAuthorize` porte un rôle lecteur sous `other-api`, avec signature, issuer, dates et audience valides. La route lecteur renvoie 403. `malformedRoleValuesDoNotAuthorize` place un nombre, un booléen, null et une chaîne vide à la place de noms de rôles valides. Ces valeurs n’obtiennent pas d’autorité par conversion en texte.

Un lecteur valide atteint la route lecteur mais échoue sur la route administrateur. Un administrateur atteint cette dernière. Examiner les autorités obtenues plutôt que déduire les permissions du nom du sujet. Ces cas prouvent la frontière du client sélectionné ; ils ne prouvent ni isolation de tenants, ni ABAC, ni droits de tous les clients Keycloak.

## Rejouer

Depuis le clone du laboratoire à la révision fixée dans le parcours, après installation locale du toolkit :

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=JwtBoundaryTest#roleForAnotherClientDoesNotAuthorize+malformedRoleValuesDoNotAuthorize+readerCannotUseAdminRoute' test
```

## Exercices corrigés

1. Le bon rôle est stocké sous un autre client. Autorise-t-il cette route ?

<details>
<summary>Correction</summary>

Non. Il est hors du client ressource configuré ; le résultat mesuré est 403.

</details>

2. Un rôle numérique peut-il devenir ROLE_42 ?

<details>
<summary>Correction</summary>

Non. Le toolkit ignore les valeurs non textuelles au lieu d’accorder une autorité obtenue par conversion.

</details>

3. Ces tests prouvent-ils l’isolation des tenants ?

<details>
<summary>Correction</summary>

Non. Cet exemple ne définit pas de politique de tenant. Il faut un contrat et des tests négatifs distincts.

</details>

## Sources et preuve

- [Tests de cette révision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/9e7fd6ed18a40588400e167424e85753d38ad406/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
