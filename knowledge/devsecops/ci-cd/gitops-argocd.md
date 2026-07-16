# GitOps with ArgoCD

## The core idea

Git is the single source of truth for *what should be running*. A controller in the cluster continuously reconciles reality to match git. You don't `kubectl apply` from your laptop — you merge a PR.

```
        git repo (desired state)
               │  ArgoCD watches
               ▼
        ┌─────────────┐   diff & sync   ┌──────────────┐
        │   ArgoCD    │ ───────────────►│  Kubernetes  │
        │ controller  │◄─────────────── │   cluster    │
        └─────────────┘   observe       └──────────────┘
```

## Why teams move to it

| Benefit | How |
|---|---|
| **Auditability** | Every change is a git commit — who, what, when, why (PR) |
| **Easy rollback** | `git revert` → controller rolls the cluster back |
| **No cluster creds on laptops** | The controller pulls; humans never push to prod directly |
| **Drift detection** | Manual `kubectl edit` in prod shows as "OutOfSync" and can auto-heal |
| **Disaster recovery** | Rebuild a cluster by pointing ArgoCD at the repo |

## Minimal Application manifest

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: notes-app
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/jihedbfr-art/notes-app-microservices
    targetRevision: main
    path: k8s/overlays/prod        # kustomize/helm path in the repo
  destination:
    server: https://kubernetes.default.svc
    namespace: notes
  syncPolicy:
    automated:
      prune: true                  # delete what's removed from git
      selfHeal: true               # revert manual drift
    syncOptions:
      - CreateNamespace=true
```

## Repo structure patterns

- **App repo vs config repo**: keep application code and deployment manifests separate — a code build updates an image tag in the config repo, which ArgoCD then deploys. Prevents a rebuild loop.
- **Environments as overlays** (Kustomize) or value files (Helm): `base/` + `overlays/{dev,staging,prod}`.

## Promotion flow

```
build image :sha → push → PR bumps tag in staging overlay → ArgoCD syncs staging
→ tests pass → PR bumps tag in prod overlay → review → merge → ArgoCD syncs prod
```
The "deploy" is a reviewable diff, not a pipeline secret firing into prod.

## Security notes

- The image tag should be an **immutable digest** or a unique `:sha`, never `:latest` — GitOps of a moving tag defeats the point.
- Secrets don't go in git in clear: use **Sealed Secrets** or **External Secrets Operator**, so the repo holds only encrypted/referenced material → see [secrets-management](../security/secrets-management.md).
- Restrict who can merge to the prod path (CODEOWNERS + branch protection) — that PR *is* your production access control now.

## Flux vs ArgoCD (one line)

Both do GitOps well. **ArgoCD** has a strong UI and app-centric model; **Flux** is lighter, more composable, CLI/GitOps-toolkit oriented. Pick one and standardize.
