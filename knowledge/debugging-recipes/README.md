# debugging-recipes

[Version française](README.fr.md)

Start with the observed symptom, then follow a focused procedure to locate and fix its cause.

## Entry structure

Exact symptom, likely causes in order of frequency, diagnosis commands and fix. See [`_TEMPLATE.md`](_TEMPLATE.md).

## Verified cases

- [Malformed Keycloak role claims](keycloak-role-claims/README.md): synthetic inputs, regression tests and the scope of the combined fix.
- [Keycloak Docker readiness](keycloak-readiness/README.md): a realm returns HTTP 200 while a healthcheck fails with 404; diagnose the management endpoint before restarting.
- [Test container image unavailable](test-container-image-unavailable.md): distinguish registry failures from application failures, then check the substitute's behavior.

## Backlog

Spring Boot startup failure, `Bean not found`, circular dependencies, `LazyInitializationException`, invalid JWTs, blocked Kafka consumers, exiting Docker containers, Kubernetes `CrashLoopBackOff`, Oracle timeouts and Postgres deadlocks. Add entries through **ajouter-entree-savoir**.

---

<div align="center">
  <img src="../../assets/brand/jihedailabs-logo.svg" alt="JihedAiLabs" width="120">
  <br>
  <sub>A <a href="https://github.com/jihedbfr-art"><b>JihedAiLabs</b></a> project</sub>
</div>
