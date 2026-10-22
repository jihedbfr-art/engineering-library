# Service-account identities

[Version française](README.fr.md) · [Course](../README.md)

The realm has four confidential clients with service accounts. The tests request tokens with `client_credentials`; there is no end-user password grant. The reader and administrator carry different roles. The no-role client has the expected audience but fails authorization; the other-audience client fails authentication even though it has reader authority in its token configuration.

Role scope mappings and service-account role assignments are both explicit. `fullScopeAllowed` stays false. The realm uses a dedicated client-role mapper and an audience mapper, avoiding a broad default scope bundle that would obscure the negative audience case.

These fixture credentials are public demonstration values. They belong only to the local realm. The sample's grant authenticates a service, not a human and not a delegated human session. Browser login, PKCE and token exchange require separate flows and tests. Do not rename a service account to suggest that a human approved an action.

## Replay

From the lab checkout at the course revision, after locally installing the toolkit:

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=KeycloakServiceAccountTest' test
```

## Worked exercises

1. Does client_credentials prove a human logged in?

<details>
<summary>Answer</summary>

No. It authenticates the configured client/service account. The lab has no delegated-human flow.

</details>

2. Which real Keycloak client distinguishes audience from role?

<details>
<summary>Answer</summary>

lab-other-audience has reader configuration but receives 401; lab-no-role has the expected audience but receives 403.

</details>

3. Why keep explicit scope mappings?

<details>
<summary>Answer</summary>

They make the permitted role set visible alongside service-account assignments; the fixture does not depend on Full Scope Allowed.

</details>

## Sources and evidence

- [Tests at this revision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/KeycloakServiceAccountTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- [Keycloak](https://www.keycloak.org/docs/latest/server_admin/index.html#_service_accounts).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
