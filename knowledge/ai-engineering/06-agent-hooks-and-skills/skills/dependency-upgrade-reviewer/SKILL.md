---
name: dependency-upgrade-reviewer
description: Review a dependency version bump (a single library, or a batch from an automated dependency-update tool) for real breaking-change risk, not just "tests still pass". Use when reviewing a dependency upgrade PR, evaluating whether to merge an automated dependency-bot PR, or asked to "check if this upgrade is safe".
---

# Dependency upgrade reviewer

Green CI on a dependency bump proves the existing test suite still passes — it doesn't prove the
upgrade is safe, because the test suite only covers what it covers. This skill's job is
evaluating the parts a passing test suite can't tell you about.

## What to check, in priority order

1. **Version jump size and semantic versioning signal.** A patch bump (`1.2.3` → `1.2.4`) carries
   a much stronger "should be safe" signal under semver than a major bump (`1.x` → `2.x`), which
   explicitly signals breaking changes are expected. Treat a major-version bump as requiring
   active changelog review, not just a CI check — and note that not every library actually
   follows semver rigorously, so the changelog is the real source of truth either way.

2. **Read the actual changelog/release notes for the version range being crossed**, not just the
   target version's notes — an upgrade from `2.1.0` to `2.5.0` crosses every intermediate
   release's changes, and a breaking change introduced in `2.3.0` still applies even though it's
   not mentioned in `2.5.0`'s own notes.

3. **Cross-reference changed/removed APIs against actual usage in the codebase.** If the
   changelog mentions a deprecated or removed method, method signature change, or changed default
   behavior, check whether the codebase actually calls it — this is the single highest-value
   check a test suite with incomplete coverage will miss entirely.

4. **Transitive dependency changes**, especially for security-sensitive libraries (crypto, auth,
   serialization) pulled in transitively — a direct dependency bump can silently change a
   transitive dependency's version too, sometimes introducing or fixing a vulnerability that
   isn't visible from the direct dependency's own changelog.

5. **Changed default behavior, not just changed/removed APIs.** The riskiest class of breaking
   change is often not a removed method (which fails loudly, a compile error or an obvious
   runtime error) but a changed default (a timeout that used to be infinite now defaults to 30s,
   a serializer that now escapes a character it didn't before) — these fail silently and are the
   hardest category for a test suite to catch unless a test happens to depend on the specific
   default.

## How to report findings

Classify as: **safe to merge** (patch/minor, changelog reviewed, no relevant API usage change),
**needs manual verification** (major bump or a changed default that affects code in this repo,
name the specific spot), or **do not merge as-is** (a used API was removed/changed incompatibly,
or a known vulnerability was introduced transitively).

## What NOT to do

- Don't treat "CI is green" as sufficient evidence for a major version bump — say explicitly what
  additional verification is needed and why the test suite alone doesn't cover it.
- Don't block a low-risk patch bump on process ceremony disproportionate to its actual risk —
  match the review depth to the version jump size and what changed, not a fixed checklist applied
  uniformly regardless of risk.
- Don't approve a batch of unrelated dependency bumps as one unit without checking each one's own
  changelog — bundling low-risk and high-risk upgrades together in one PR is itself worth flagging
  as a process issue, since it makes isolating which upgrade caused a regression harder later.
