# Signature et émetteur

[English version](README.md) · [Parcours](../README.fr.md)

Un JWT lisible reste une entrée non fiable. Ce chapitre utilise une clé RSA aléatoire et un JWKS de loopback ; l’API vérifie une signature avant que les règles de rôles puissent intervenir. L’issuer de test utilise un domaine `.invalid` : il est comparé comme claim, sans contacter ce domaine.

`wrongIssuerWithSameSigningKeyIsRejected` ne change que l’issuer d’un jeton signé par la clé approuvée. `untrustedSignatureIsRejected` conserve l’issuer et les rôles attendus, mais signe avec une autre clé sous le même identifiant. Les deux requêtes renvoient 401. Le rôle `reader` ne répare aucun de ces refus.

L’URL des JWK est une configuration de confiance de l’application, pas une URL récupérée dans le jeton reçu. Lire le décodeur de l’exemple avant de diagnostiquer un issuer incorrect. Le serveur de clés local établit une frontière de validation ; il ne fournit ni discovery OIDC ni service de rotation.

## Rejouer

Depuis le clone du laboratoire à la révision fixée dans le parcours, après installation locale du toolkit :

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=JwtBoundaryTest#wrongIssuerWithSameSigningKeyIsRejected+untrustedSignatureIsRejected' test
```

## Exercices corrigés

1. Décoder le payload prouve-t-il la signature ?

<details>
<summary>Correction</summary>

Non. La requête doit passer une validation avec une clé publique approuvée ; lire les claims reste une inspection.

</details>

2. Pourquoi garder la même clé dans le test d’issuer ?

<details>
<summary>Correction</summary>

Pour ne pas expliquer le 401 par une autre clé. Ce test isole l’issuer incorrect.

</details>

3. Cela prouve-t-il la rotation JWKS ?

<details>
<summary>Correction</summary>

Non. Le jeu de clés publié reste fixe. Rotation et récupération sur clé inconnue demandent d’autres tests.

</details>

## Sources et preuve

- [Tests de cette révision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
