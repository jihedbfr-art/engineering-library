# Client roles and authority mapping

[Version française](README.fr.md) · [Course](../README.md)

This lab maps roles from `resource_access.toolkit-lab.roles`. Realm-role mapping is disabled. The filter chain explicitly uses the toolkit converter; endpoint rules then ask for `ROLE_READER` or `ROLE_ADMIN`.

`roleForAnotherClientDoesNotAuthorize` carries a reader role under `other-api` with otherwise valid signature, issuer, dates and audience. The reader endpoint returns 403. `malformedRoleValuesDoNotAuthorize` supplies a number, a boolean, null and an empty string where valid role names should be. Those values do not acquire authority through string conversion.

A valid reader reaches the reader endpoint but fails on the admin endpoint. An administrator reaches the admin endpoint. Inspect the output authorities rather than inferring permissions from a username. These cases prove the sample's selected client boundary; they do not establish tenant isolation, ABAC or permissions for every Keycloak client.

## Replay

From the lab checkout at the course revision, after locally installing the toolkit:

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=JwtBoundaryTest#roleForAnotherClientDoesNotAuthorize+malformedRoleValuesDoNotAuthorize+readerCannotUseAdminRoute' test
```

## Worked exercises

1. The correct role is stored under another client. Does it authorize this route?

<details>
<summary>Answer</summary>

No. It is outside the configured resource client; the measured result is 403.

</details>

2. May a numeric role become ROLE_42?

<details>
<summary>Answer</summary>

No. The toolkit ignores non-string entries rather than granting a stringified authority.

</details>

3. Does passing these tests prove tenant isolation?

<details>
<summary>Answer</summary>

No. There is no tenant policy in this sample. That requires a separate contract and negative tests.

</details>

## Sources and evidence

- [Tests at this revision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/9e7fd6ed18a40588400e167424e85753d38ad406/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
