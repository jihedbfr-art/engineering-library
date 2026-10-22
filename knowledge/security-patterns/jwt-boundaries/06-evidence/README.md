# Evidence and safe reproduction

[Version française](README.fr.md) · [Course](../README.md)

Use the smallest proof that answers the question, then state its boundary. The converter's tests establish mapping on supplied claims. The signed-JWT suite adds real signature/claim validation and HTTP. The Keycloak suite adds real issuance. The smoke script adds the built executable JAR and Compose wiring.

The smoke checker prints six scenario names and statuses, not access tokens. Its test project binds loopback ports; the local validation used free ports and stopped only its own Java process and Compose resources. The public example documents its default ports and a project-scoped stop command.

A successful JAR run does not establish production readiness. Key rotation, revocation, browser sessions, MFA, tenant separation and MCP transport are outside this lab. Separate an untested hypothesis from an observed defect. Preserve the code revision, commands, test counts, platform and limits when handing the work to another engineer.

## Replay

Follow the [lab](https://github.com/jihedbfr-art/spring-keycloak-toolkit/tree/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server) terminals and prerequisites. After building and starting local Keycloak and the JAR, from `examples/resource-server`:

```bash
python smoke.py
```

## Worked exercises

1. Which proof adds packaging to the HTTP tests?

<details>
<summary>Answer</summary>

Running the built JAR and the six smoke scenarios, rather than only starting the test application.

</details>

2. May a green JWT lab be reported as an MCP authorization test?

<details>
<summary>Answer</summary>

No. This lab has no MCP transport or tool invocation.

</details>

3. What should a reproducible handoff include?

<details>
<summary>Answer</summary>

Exact commit, commands, runtime versions, results and limits; no live tokens or private production inputs.

</details>

## Sources and evidence

- [Tests at this revision](https://github.com/jihedbfr-art/spring-keycloak-toolkit/blob/dc2e74e12dafbc337ea94848694fec1bbf1631b6/examples/resource-server/src/test/java/lab/JwtBoundaryTest.java).
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

---

<div align="center">
  <img src="../../../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
