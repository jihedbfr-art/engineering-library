# Supply-Chain Security

The uncomfortable truth: most of the code running in production isn't yours. It's dependencies, and dependencies of dependencies, pulled in by a build tool that trusts a registry that trusts whoever last published under that package name. SolarWinds, the `xz` backdoor, the endless stream of typosquatted npm packages — none of those broke in through application code review, they broke in through the supply chain nobody was watching.

## What's actually in scope

- **Build-time**: compromised dependency, malicious build script (`postinstall` hooks are the classic vector), poisoned CI runner.
- **Artifact**: the image/jar/binary itself tampered with between build and deploy.
- **Runtime**: a legitimate dependency that phones home or exfiltrates data it was never supposed to touch.

Most teams only think about the first one, and even then only reactively (a CVE alert on a `pom.xml` dependency). The other two are where the harder incidents come from.

## SBOM — know what you actually ship

A Software Bill of Materials is the inventory: every dependency, transitive included, with version and license. Without one, "are we affected by CVE-2024-XXXX" is a grep-and-pray exercise across every repo instead of a query against a known list.

```bash
# Generate an SBOM for a Maven project (CycloneDX format)
mvn org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
```
```bash
# Generate one for a container image
syft <image>:<tag> -o cyclonedx-json > sbom.json
```

Generate it at build time, store it alongside the artifact, and keep it — an SBOM you regenerate on demand when a CVE drops is already too late for the incident it was supposed to answer.

## Dependency pinning and verification

- **Pin exact versions**, not ranges (`1.2.3`, not `^1.2.0`) — a range lets a compromised patch release into your build automatically, no review, no diff.
- **Lockfiles committed to git** (`package-lock.json`, Maven's effective dependency tree via `mvn dependency:tree` snapshotted) — the build should be reproducible from the lockfile alone, not from "whatever the registry serves today."
- **Checksum/signature verification** where the ecosystem supports it (`npm audit signatures`, Maven's GPG-signed artifacts on Central) — catches a registry compromise or a mirror serving tampered bytes, which a version pin alone doesn't.

## SLSA — thinking about build provenance

SLSA (Supply-chain Levels for Software Artifacts) gives a framework for "can I prove this artifact came from this source code, built by this pipeline, unmodified since." The levels go from "basic hygiene" (build is scripted, not manual) up to "the build is fully hermetic and every step is independently verifiable." Most teams don't need to chase the top level — but even getting to "the artifact I deployed has an attestation tying it back to a specific commit and pipeline run" closes off a whole class of "how did this binary get into prod" incidents.

```yaml
# GitHub Actions: generate provenance attestation for a build artifact
- uses: actions/attest-build-provenance@v1
  with:
    subject-path: 'target/*.jar'
```

## The dependency-review gate that actually catches things

```yaml
# GitHub Actions — blocks a PR that introduces a dependency with a known
# high/critical vuln, or a license the org doesn't allow
- uses: actions/dependency-review-action@v4
  with:
    fail-on-severity: high
```

This is the cheapest control on this whole page to add and the one most repos are missing — it runs on every PR, catches the problem before merge instead of after a scheduled scan finds it three weeks later.

## Where this fits with the rest of devsecops

Supply-chain scanning is a different failure mode from [SAST/DAST](sast-dast.md) (which look at code *you* wrote) and from [secrets-management.md](secrets-management.md) (which is about credentials leaking, not code being tampered with). All three belong in CI, but they're catching genuinely different things — don't let "we run Trivy" stand in as "we do supply-chain security," it's one piece.

## TODO

Haven't written up container base-image provenance in detail (verifying a base image against its published digest rather than trusting `latest` — `docker pull image@sha256:...` vs `docker pull image:latest`) — that's really its own topic tangled up with [docker-hardening.md](../containers/docker-hardening.md), worth splitting out properly rather than cramming a paragraph in here.
