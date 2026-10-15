# Unexpected authorities or conversion failure from Keycloak role claims

[Version française](README.fr.md)

A JWT role converter grants `ROLE_NULL` or `ROLE_123`, or raises `IllegalArgumentException` when an access claim has the wrong JSON shape. The symptoms can be reproduced without issuing a real token: construct a `Jwt` with synthetic claims and invoke the converter directly.

## Locate the fault

| Symptom | Verified cause in the toolkit | Fix |
|---|---|---|
| Numbers, booleans or null become authorities | `roles.stream().map(String::valueOf)` converts every list entry to text | Keep only non-blank strings |
| A list in `realm_access` raises an exception | `getClaimAsMap` tries to convert a non-map claim | Read the raw claim and check `instanceof Map<?, ?>` |
| A malformed realm claim prevents valid client roles from being read | Realm extraction fails before resource extraction | Ignore the malformed access object and continue extracting from the valid one |

The chosen policy ignores malformed role data. Token rejection is a separate validation policy. A converter test checks role mapping; it does not validate a token's signature or prove that a caller can obtain a trusted token with those claims.

## Reproduce the two failures

In a unit test, construct these claims with `Jwt.withTokenValue("dummy-token")`, a synthetic header and a short lifetime. Never use a production token as a fixture.

```json
{
  "realm_access": {"roles": ["viewer", 123, true, null, " "]},
  "resource_access": {"my-client": {"roles": ["editor", 456]}}
}
```

Before the first fix, the converter granted `ROLE_123`, `ROLE_TRUE`, `ROLE_NULL`, `ROLE_456` and a whitespace role alongside the valid roles. The assertion should require exactly `ROLE_VIEWER` and `ROLE_EDITOR`.

```json
{
  "realm_access": ["admin"],
  "resource_access": "unexpected"
}
```

Before the second fix, conversion raised `IllegalArgumentException` on `realm_access`. The assertion should require an empty authority collection with no exception. Add another test with malformed `realm_access` and a valid resource role: `ROLE_EDITOR` must still be returned.

## Check the corrections

Both fixes are merged into the toolkit's `main` branch. In a new checkout:

```bash
git clone https://github.com/jihedbfr-art/spring-keycloak-toolkit.git
cd spring-keycloak-toolkit
mvn -q -Dtest=KeycloakRealmRoleConverterTest test
```

[PR #10](https://github.com/jihedbfr-art/spring-keycloak-toolkit/pull/10) filters role entries. [PR #11](https://github.com/jihedbfr-art/spring-keycloak-toolkit/pull/11) checks access-object shape and removes the unchecked client-map cast. Each regression test failed before its correction. The test class keeps all three: `ignoresNonStringAndBlankRoleValues`, `ignoresMalformedAccessClaims` and `keepsValidResourceRolesWhenRealmClaimIsMalformed`. Run `mvn -q verify` for the whole tree; it needs Docker for the Keycloak tests.

## Evidence and limits

On 14 October 2026, both code corrections together passed 25 local tests, including Keycloak 26.7.1 under Docker, with Spring Boot 4.1.0 and Java 17. The build's JaCoCo gate passed. The two separate PRs each passed five remote compatibility jobs. The combined local test covered one Java/Spring combination; it is not a full combined compatibility matrix or a production validation. After both fixes were merged on 15 October 2026, the combined tree ran 27 local tests, five of them under Docker, and its final head passed the five remote compatibility jobs.

Exact changes: [role-value filtering](https://github.com/jihedbfr-art/spring-keycloak-toolkit/commit/6026018), [access-object checking](https://github.com/jihedbfr-art/spring-keycloak-toolkit/commit/7055489).

---

<div align="center">
  <img src="../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
