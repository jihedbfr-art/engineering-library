# Autorités inattendues ou échec de conversion des rôles Keycloak

[English version](README.md)

Un convertisseur de rôles JWT accorde `ROLE_NULL` ou `ROLE_123`, ou lève une `IllegalArgumentException` lorsque la structure JSON d'un claim d'accès est incorrecte. Ces symptômes se reproduisent sans émettre de véritable jeton : construire un `Jwt` avec des claims synthétiques et appeler directement le convertisseur.

## Localiser le défaut

| Symptôme | Cause vérifiée dans le toolkit | Correctif |
|---|---|---|
| Des nombres, booléens ou null deviennent des autorités | `roles.stream().map(String::valueOf)` transforme chaque entrée en texte | Conserver uniquement les chaînes non vides |
| Une liste dans `realm_access` provoque une exception | `getClaimAsMap` tente de convertir un claim qui n'est pas une map | Lire le claim brut et vérifier `instanceof Map<?, ?>` |
| Un claim realm malformé empêche de lire les rôles client valides | L'extraction realm échoue avant l'extraction resource | Ignorer l'objet d'accès malformé et poursuivre sur celui qui est valide |

La politique retenue ignore les données de rôles malformées. Le rejet du jeton relève d'une politique de validation distincte. Un test du convertisseur vérifie le mapping des rôles ; il ne valide pas la signature du jeton et ne prouve pas qu'un appelant peut obtenir un jeton accepté avec ces claims.

## Reproduire les deux défauts

Dans un test unitaire, construire ces claims avec `Jwt.withTokenValue("dummy-token")`, un header synthétique et une durée de validité courte. Ne jamais utiliser un jeton de production comme fixture.

```json
{
  "realm_access": {"roles": ["viewer", 123, true, null, " "]},
  "resource_access": {"my-client": {"roles": ["editor", 456]}}
}
```

Avant le premier correctif, le convertisseur accordait `ROLE_123`, `ROLE_TRUE`, `ROLE_NULL`, `ROLE_456` et un rôle composé d'espaces, en plus des rôles valides. L'assertion doit exiger exactement `ROLE_VIEWER` et `ROLE_EDITOR`.

```json
{
  "realm_access": ["admin"],
  "resource_access": "unexpected"
}
```

Avant le second correctif, la conversion levait une `IllegalArgumentException` sur `realm_access`. L'assertion doit exiger une collection d'autorités vide, sans exception. Ajouter un autre test avec un `realm_access` malformé et un rôle resource valide : `ROLE_EDITOR` doit être conservé.

## Vérifier les corrections proposées

Utiliser un nouveau clone. Les versions ci-dessous sont les références du code examiné, pas des tags de release.

```bash
git clone https://github.com/jihedbfr-art/spring-keycloak-toolkit.git
cd spring-keycloak-toolkit
git checkout 6026018
mvn -q -Dtest=KeycloakRealmRoleConverterTest test
git checkout 920441e
mvn -q -Dtest=KeycloakRealmRoleConverterTest test
```

La [PR #10](https://github.com/jihedbfr-art/spring-keycloak-toolkit/pull/10) filtre les entrées des listes de rôles. La [PR #11](https://github.com/jihedbfr-art/spring-keycloak-toolkit/pull/11) vérifie la structure des objets d'accès et supprime le cast non vérifié de la map client. Chaque test de régression échouait avant sa correction.

Ces versions appartiennent à deux branches distinctes. Pour les combiner, conserver les trois tests `ignoresNonStringAndBlankRoleValues`, `ignoresMalformedAccessClaims` et `keepsValidResourceRolesWhenRealmClaimIsMalformed`. Résoudre les ajouts concurrents des README et des tests sans perdre l'un des comportements, puis exécuter `mvn -q verify` sur l'arbre final.

## Preuves et limites

Le 14 octobre 2026, les deux correctifs de code combinés ont passé 25 tests locaux, dont l'intégration Keycloak 26.7.1 sous Docker, avec Spring Boot 4.1.0 et Java 17. Le seuil JaCoCo du build a passé. Chacune des deux PR a passé cinq jobs distants de compatibilité. Le test local combiné couvre une seule combinaison Java/Spring ; il ne constitue ni une matrice complète combinée ni une validation de production.

Changements exacts : [filtrage des valeurs de rôles](https://github.com/jihedbfr-art/spring-keycloak-toolkit/commit/6026018), [contrôle des objets d'accès](https://github.com/jihedbfr-art/spring-keycloak-toolkit/commit/7055489).

---

<div align="center">
  <img src="../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>Un projet <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a></sub>
</div>
