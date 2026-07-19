# 🛡️ DevSecOps

The full pipeline: build, secure, ship, observe.

- [ci-cd/](ci-cd/) — [GitHub Actions secure pipeline](ci-cd/github-actions.md), [GitOps with ArgoCD](ci-cd/gitops-argocd.md)
- [containers/](containers/) — Docker, Kubernetes, image hardening
- [security/](security/) — OWASP Top 10, SAST/DAST, secrets management, [supply-chain security](security/supply-chain-security.md)
- [iac/](iac/) — [Terraform](iac/terraform-basics.md), [Ansible](iac/ansible-basics.md), immutable infrastructure
- [monitoring/](monitoring/) — [Prometheus/Grafana/logging](monitoring/observability.md), [distributed tracing](monitoring/distributed-tracing.md), alerting

## The DevSecOps loop

Plan → Code → Build → **Test & Scan** → Release → Deploy → **Operate & Monitor** → feedback → Plan

Security gates live at every arrow, not just one.
