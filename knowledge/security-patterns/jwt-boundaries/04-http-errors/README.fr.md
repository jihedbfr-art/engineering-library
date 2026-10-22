# Diagnostiquer 401 et 403

[English version](README.md) · [Parcours](../README.fr.md)

Partir de l’échange HTTP observé. Sans jeton, la route lecteur renvoie 401, un challenge Bearer et un corps Problem Details. Avec un jeton lecteur valide, la route administrateur renvoie 403. Ces résultats pointent vers des étapes différentes du pipeline de cet exemple.

Le refus d’issuer ou d’audience intervient pendant l’authentification JWT. Une autorité requise absente intervient après acceptation du jeton, pendant l’autorisation. Cette distinction ne remplace pas l’examen des logs et validateurs : le statut seul n’identifie pas le claim précisément refusé.

Le laboratoire branche ses handlers Problem Details sur le resource server et la gestion générale des exceptions. Les tests traversent un vrai serveur HTTP, pas seulement une méthode de conversion. Corps et en-tête sont vérifiés pour l’absence de jeton ; cela ne promet ni les mêmes attributs d’erreur pour tous les jetons invalides, ni une conformité RFC générale.

## Rejouer

Depuis le clone du laboratoire à la révision fixée dans le parcours, après installation locale du toolkit :

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=JwtBoundaryTest#missingTokenHasBearerChallengeAndProblemDetail+readerCannotUseAdminRoute+malformedBearerIsRejected' test
```

## Exercices corrigés

1. Quelles preuves joindre à un diagnostic 401 ?

<details>
<summary>Correction</summary>

Route, statut, challenge et corps expurgé, ainsi que le test ou validateur qui isole la cause. Ne pas coller de Bearer réel.

</details>

2. Un jeton lecteur atteint la route administrateur et reçoit 403. Faut-il changer l’issuer ?

<details>
<summary>Correction</summary>

Pas sur cette preuve. Dans ce cas mesuré, l’exigence de rôle explique le refus, pas l’issuer.

</details>

3. Un test unitaire du convertisseur prouve-t-il le challenge HTTP ?

<details>
<summary>Correction</summary>

Non. Le test HTTP doit vérifier cet en-tête sur le trajet réel de la chaîne de filtres.

</details>

## Sources et preuve

- [Tests de cette révision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- [RFC 6750 §3](https://www.rfc-editor.org/rfc/rfc6750#section-3).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
