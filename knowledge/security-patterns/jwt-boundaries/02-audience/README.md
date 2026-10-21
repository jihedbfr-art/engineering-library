# Audience is a separate boundary

[Version française](README.fr.md) · [Course](../README.md)

The sample accepts audience `toolkit-lab`. Issuer says who issued a token; audience says which recipient it targets. Neither a role nor a client name substitutes for the audience check.

The first implementation of this lab configured issuer and time validation without an audience validator. Two signed-token tests then observed 200 for a missing or different audience. Adding a composed audience validator changed both results to 401 while preserving the positive reader case. This is an application configuration lesson, not a claim that the role-conversion library supplies audience policy.

The Keycloak fixture separates its audience mapper from its role mapper. `lab-other-audience` still has the reader role but targets `other-api`; its HTTP request is rejected. Keep that negative client when changing the realm. Removing it would make it easier to mistake successful role mapping for complete token validation.

## Replay

From the lab checkout at the course revision, after locally installing the toolkit:

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=JwtBoundaryTest#wrongAudienceIsRejectedDespiteReaderRole+missingAudienceIsRejected+validReaderIsAccepted' test
```

## Worked exercises

1. A correctly signed reader token targets another API. Which result does this lab require?

<details>
<summary>Answer</summary>

401, before the reader role can authorize this resource.

</details>

2. Can a missing audience become an implicit match?

<details>
<summary>Answer</summary>

No. The sample rejects a missing audience; its negative test must stay red if that check is removed.

</details>

3. Where are audience policy and role conversion configured?

<details>
<summary>Answer</summary>

The consumer application composes the JWT validators. The toolkit converts the chosen client roles after token validation.

</details>

## Sources and evidence

- [Tests at this revision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/9e7fd6ed18a40588400e167424e85753d38ad406/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- [Keycloak](https://www.keycloak.org/docs/latest/server_admin/index.html#_service_accounts).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
