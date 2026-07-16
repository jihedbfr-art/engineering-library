# 🛡️ DevSecOps

The full pipeline: build, secure, ship, observe.

- [ci-cd/](ci-cd/) — [GitHub Actions secure pipeline](ci-cd/github-actions.md), [GitOps with ArgoCD](ci-cd/gitops-argocd.md)
- [containers/](containers/) — Docker, Kubernetes, image hardening
- [security/](security/) — OWASP Top 10, SAST/DAST, secrets management, supply-chain security
- [iac/](iac/) — Terraform, Ansible, immutable infrastructure
- [monitoring/](monitoring/) — Prometheus, Grafana, logging, alerting

## The DevSecOps loop

Plan → Code → Build → **Test & Scan** → Release → Deploy → **Operate & Monitor** → feedback → Plan

Security gates live at every arrow, not just one.
