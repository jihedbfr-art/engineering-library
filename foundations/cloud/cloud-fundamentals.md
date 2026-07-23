# Cloud Fundamentals

## Why cloud

Pay for computing as a utility instead of buying servers: **elastic** (scale up/down on demand), **global** (deploy near users), **managed** (someone else patches the database), **pay-as-you-go**. The trade: recurring cost and vendor lock-in vs upfront capital and ops burden.

## Service models

```
On-prem      IaaS         PaaS         SaaS
you manage → you manage → you manage → you use
everything   OS & up      just code    nothing (Gmail)
```

| Model | You manage | Example |
|---|---|---|
| **IaaS** | OS, runtime, app | EC2, Compute Engine — virtual machines |
| **PaaS** | just your app | App Engine, Heroku, Cloud Run |
| **Serverless / FaaS** | just functions | Lambda, Cloud Functions — pay per execution |
| **SaaS** | nothing | you're the user, not the operator |

Higher up = less control, less ops. Choose the highest level that still meets your needs.

## The core services (every cloud has these)

| Need | AWS | Azure | GCP |
|---|---|---|---|
| Virtual machines | EC2 | Virtual Machines | Compute Engine |
| Object storage | S3 | Blob Storage | Cloud Storage |
| Managed SQL | RDS | SQL Database | Cloud SQL |
| Serverless functions | Lambda | Functions | Cloud Functions |
| Containers (managed k8s) | EKS | AKS | GKE |
| Networking (private net) | VPC | VNet | VPC |
| Identity & access | IAM | Entra ID | IAM |

Learn the *concepts* once; the names map across providers.

## Mental models that prevent disasters

1. **Everything is IAM.** Access is controlled by policies. Most cloud breaches are misconfigured permissions or public storage buckets → least privilege, always.
2. **The network is yours to design.** A VPC with private subnets; only load balancers face the internet; databases never do.
3. **Regions & availability zones.** Deploy across AZs for resilience; pick regions near users for latency and for data-residency laws.
4. **Managed > self-hosted** unless you have a strong reason. Let the provider run the database's backups, patching, and failover.

## Cost — the thing that surprises everyone

- **Egress (data leaving) costs money**; ingress is usually free. Big data transfers between regions/clouds add up fast.
- **Idle resources still bill.** Turn off dev environments; delete orphaned disks and IPs.
- **Serverless is cheap at low/spiky traffic, expensive at constant high traffic** — the opposite of VMs. Match the model to the load.
- Set **budgets and alerts** on day one. Tag resources by team/project for accountability.

## The well-architected mindset

Reliability (design for failure), Security (least privilege, encryption), Cost optimization, Performance, Operational excellence (automate, monitor). Every design decision trades among these — name the trade-off, don't pretend it's free. See [IaC](../devsecops/iac/terraform-basics.md) to manage all of this reproducibly.
