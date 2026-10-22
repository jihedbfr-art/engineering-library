# Diagnose 401 and 403

[Version française](README.fr.md) · [Course](../README.md)

Start with the observed HTTP exchange. A missing token on the reader endpoint returns 401, a Bearer challenge and a Problem Details body. A valid reader token on the admin endpoint returns 403. The two outcomes point to different places in this sample's pipeline.

An issuer or audience rejection happens during JWT authentication. A missing required authority happens after an accepted token reaches authorization. This distinction does not replace checking logs and validators: a status alone cannot identify the precise failed claim.

The lab wires its Problem Details handlers into both resource-server and general exception handling. The tests cross a running HTTP server, not only a converter method. The body and header are checked on a missing-token request; that does not promise identical error attributes for every invalid-token variant or a universal RFC conformance result.

## Replay

From the lab checkout at the course revision, after locally installing the toolkit:

```bash
mvn -f examples/resource-server/pom.xml '-Dtest=JwtBoundaryTest#missingTokenHasBearerChallengeAndProblemDetail+readerCannotUseAdminRoute+malformedBearerIsRejected' test
```

## Worked exercises

1. What evidence should accompany a 401 report?

<details>
<summary>Answer</summary>

Route, status, challenge and sanitized body, plus the test or validator that isolates the cause. Never paste a live Bearer token.

</details>

2. A reader token reaches an admin route and returns 403. Must the issuer be changed?

<details>
<summary>Answer</summary>

Not on that evidence. In this measured case the role requirement, not the issuer, explains the refusal.

</details>

3. Does a converter unit test establish an HTTP challenge?

<details>
<summary>Answer</summary>

No. The HTTP test must check that header on the actual filter-chain path.

</details>

## Sources and evidence

- [Tests at this revision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- [RFC 6750 §3](https://www.rfc-editor.org/rfc/rfc6750#section-3).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
