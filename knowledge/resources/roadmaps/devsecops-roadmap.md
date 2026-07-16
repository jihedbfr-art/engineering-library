# DevSecOps Roadmap

From developer to DevSecOps engineer — each stage builds on the previous one.

```
Stage 1          Stage 2         Stage 3          Stage 4            Stage 5
FOUNDATIONS  →   CONTAINERS  →   CI/CD & SEC  →   ORCHESTRATION  →   SCALE & SRE
Linux, git,      Docker,         pipelines,       Kubernetes,        observability,
networking,      compose,        SAST/DAST,       Helm, GitOps,      SLOs, chaos,
one language     registries      secrets, IaC     service mesh       platform eng
```

## Stage 1 — Foundations (skip nothing here)

- **Linux**: filesystem, permissions, processes, systemd, networking tools → [linux cheatsheet](../cheatsheets/linux.md)
- **Networking**: TCP/IP, DNS, HTTP(S), TLS handshake, ports, firewalls
- **Git**: branching, rebasing, PR workflow → [git cheatsheet](../cheatsheets/git.md)
- **One scripting language well**: Python or Bash (you'll automate everything)
- ✅ *You can*: deploy a web app on a bare Linux VM with nginx + TLS, by hand.

## Stage 2 — Containers

- Docker: images, layers, volumes, networks, compose → [docker-hardening](../../devsecops/containers/docker-hardening.md)
- Image security: minimal bases, non-root, scanning (Trivy)
- ✅ *You can*: containerize a 3-service app with compose, images < 200MB, all non-root.

## Stage 3 — CI/CD & Security integration

- GitHub Actions / GitLab CI → [github-actions](../../devsecops/ci-cd/github-actions.md)
- The security gates: SAST, SCA, secret scanning, image scan, DAST → [sast-dast](../../devsecops/security/sast-dast.md)
- Secrets management → [secrets-management](../../devsecops/security/secrets-management.md)
- IaC: Terraform basics + scanning → [terraform-basics](../../devsecops/iac/terraform-basics.md)
- ✅ *You can*: a push to main deploys to staging through a pipeline that blocks on CRITICAL vulns.

## Stage 4 — Orchestration

- Kubernetes core objects, probes, RBAC, NetworkPolicies → [kubernetes-essentials](../../devsecops/containers/kubernetes-essentials.md)
- Helm charts; GitOps (ArgoCD/Flux) — the cluster state lives in git
- Admission policies (Kyverno): enforce security by default
- ✅ *You can*: run your app on k8s with zero-downtime deploys and a rollback that takes one command.

## Stage 5 — Scale & reliability

- Observability: metrics, logs, traces, SLOs → [observability](../../devsecops/monitoring/observability.md)
- Incident response, post-mortems, error budgets
- Cost awareness, multi-env strategy, platform engineering mindset
- ✅ *You can*: define and defend an SLO, and lead a blameless post-mortem.

## Meta-advice

1. **Build a lab, not a certificate wall.** One real project through all 5 stages beats 10 courses.
2. Automate everything you did twice.
3. Read post-mortems of real outages (many are public) — free experience.
4. Your GitHub is your CV: pipelines, IaC and write-ups included.
