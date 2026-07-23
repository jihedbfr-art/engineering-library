# Managed Containers — Comparing the Options

You know [Docker](../devsecops/containers/docker-hardening.md) and [Kubernetes](../devsecops/containers/kubernetes-essentials.md). Now: where do you actually *run* the containers, and how much of the underlying machinery do you want to own?

## The spectrum, from most control to least

```
Self-managed k8s  →  Managed k8s control plane  →  Serverless containers  →  Serverless functions
(you run every-      (cloud runs the control        (no cluster at all,       (no container even,
thing, incl. the      plane, you run nodes)          just "run my image")      just a function)
control plane)
      │                       │                             │                        │
  most control            balance                     least ops burden        least control
  most ops work                                        least flexibility      most constraints
```

## Managed Kubernetes — EKS vs AKS vs GKE

| | **EKS** (AWS) | **AKS** (Azure) | **GKE** (GCP) |
|---|---|---|---|
| Control plane cost | Paid (~$0.10/hr) | **Free** | Paid (Autopilot has its own pricing model) |
| Ease of setup | Moderate — more manual wiring | Easy, especially in a Microsoft shop | **Easiest** — Autopilot mode near-fully manages nodes too |
| Ecosystem maturity | Largest overall AWS ecosystem around it | Strong if already on Entra ID/Azure | Deepest Kubernetes-native tooling (Google invented k8s) |
| Best fit | Already deep in AWS services | Already deep in Microsoft/Entra ID | Want the smoothest k8s experience specifically |

Honest take: if you're not already committed to one cloud for other reasons, **GKE Autopilot** is the easiest on-ramp to running real Kubernetes without a dedicated platform team. But "which cloud" is usually decided by where the rest of your infrastructure already lives, not by which managed-k8s offering is marginally nicer — don't let this one factor drive the whole cloud decision.

## Serverless containers — Cloud Run vs Fargate vs Container Apps

| | **Cloud Run** (GCP) | **Fargate** (AWS, via ECS/EKS) | **Container Apps** (Azure) |
|---|---|---|---|
| Model | Give it an image, get a URL | A launch type for ECS/EKS tasks | Give it an image, get an app |
| Scale to zero | ✅ Yes, and it's simple | ⚠️ Possible but more setup | ✅ Yes |
| Complexity | **Lowest** — closest to "just deploy" | Higher — still thinks in ECS/EKS concepts | Low-moderate |
| Good for | Stateless APIs, webhooks, simple services | Teams already using ECS/EKS who want to drop node management | Azure-first teams wanting Cloud-Run-like simplicity |

If the entire requirement is "run this container, scale it, don't make me think about servers or clusters" — **Cloud Run** is the simplest mental model of the three. Fargate is more powerful (integrates with the full ECS/EKS ecosystem) but that power comes with more concepts to learn upfront.

## When to skip Kubernetes entirely

A genuinely common mistake: reaching for full Kubernetes (self-managed or managed) for a handful of simple stateless services. If your workload is "some APIs and a worker or two," serverless containers (Cloud Run/Fargate/Container Apps) get you 90% of the benefit — containerized, scalable, portable — with a fraction of the operational surface area. Reach for real Kubernetes when you need: fine-grained scheduling control, complex networking (service mesh, custom CNI), stateful workloads with specific storage needs, or you're already running enough services that the platform investment pays for itself.

## The portability question

All of the above run standard OCI container images — the same [Dockerfile](../devsecops/containers/docker-hardening.md) works everywhere. What's *not* portable is everything around the container: IAM/identity model, networking primitives, managed database connection strings, secrets integration. "Just use containers, we're cloud-agnostic" is true for the image and false for everything wired around it — plan for that reality rather than the marketing version of it.
