# GCP Essentials

Same pattern as [AWS](aws-essentials.md) and [Azure](azure-essentials.md). GCP's identity is data/AI-heavy (BigQuery, Vertex AI) and generally considered the smoothest Kubernetes experience of the three — unsurprising, since Google invented Kubernetes.

## Compute

- **Compute Engine** — the EC2/VM equivalent.
- **Cloud Functions** — serverless functions, the Lambda equivalent.
- **Cloud Run** — serverless *containers*: give it a container image, it scales on requests (including to zero). Arguably the best of the three clouds' "just run my container, don't make me think about infra" offerings — simpler to reason about than Fargate or Container Apps for a straightforward stateless service.
- **GKE (Google Kubernetes Engine)** — managed Kubernetes; GKE Autopilot mode manages node provisioning for you entirely, the closest any cloud gets to "Kubernetes with none of the cluster-ops burden."

## Storage

- **Cloud Storage** — the S3 equivalent. Storage classes (Standard/Nearline/Coldline/Archive) for cost-tiering by access frequency.
- **Persistent Disk** — the EBS equivalent.
- **Filestore** — managed NFS.

## Database

- **Cloud SQL** — managed Postgres/MySQL/SQL Server, the RDS equivalent.
- **BigQuery** — GCP's standout: a fully serverless data warehouse, query petabytes with plain SQL, pay per query scanned (or flat-rate). The default answer to "where do our analytics live" in a GCP shop → [data warehouses](../data-engineering/data-warehouses.md).
- **Firestore** — managed NoSQL document database with real-time listeners, popular for mobile/web apps that want live-updating data with minimal backend code.
- **Spanner** — globally distributed, strongly consistent SQL database — genuinely rare combination (most distributed databases trade away strong consistency); expensive, but solves a real problem for global-scale transactional systems.
- **Memorystore** — managed Redis/Memcached.

## Networking

- **VPC** — same concept as AWS/Azure, but GCP's VPCs are **global by default** (subnets span regions within one VPC) — a genuine structural difference, not just naming.
- **Cloud DNS** — DNS hosting.
- **Cloud CDN** — edge caching, the CloudFront/Front Door equivalent.
- **Cloud Load Balancing** — global L7/L4 load balancing, one anycast IP in front of a worldwide deployment.
- **Apigee / API Gateway** — managed API gateway (Apigee for enterprise-grade API management, API Gateway for lighter use).

## Identity & operations

- **IAM** — same core idea as AWS: least privilege, roles over keys, no long-lived credentials where avoidable.
- **Cloud Monitoring / Cloud Logging** (formerly Stackdriver) → [observability](../devsecops/monitoring/observability.md).
- **Deployment Manager** exists but most teams use [Terraform](../devsecops/iac/terraform-basics.md) — GCP's own IaC tooling has less mindshare than AWS's CDK or Azure's Bicep.

## Vertex AI — worth knowing exists

GCP's unified ML/AI platform: managed training, hosted foundation models, feature stores, and the pipeline tooling to productionize models — relevant if [ML fundamentals](../ai/01-foundations/ml-fundamentals.md) or [LLM apps](../ai/01-foundations/llm-fundamentals.md) work needs a managed platform instead of hand-rolled infrastructure.

## A typical web app on GCP

```
Cloud DNS
   → Cloud CDN
   → Cloud Load Balancing (global anycast IP)
      → Cloud Run or GKE Autopilot (app)
         → Cloud SQL (database, private IP)
         → Memorystore (Redis)
   IAM gates every service · Cloud Monitoring watches it all
```

## Starter safety checklist

- [ ] Organization policies + IAM Conditions set before real workloads land.
- [ ] No service account keys downloaded/committed — use Workload Identity where possible.
- [ ] Cloud Storage buckets: uniform bucket-level access on, public access prevention enforced.
- [ ] Budget alerts configured — BigQuery in particular can surprise you if a query scans a huge unfiltered table.
- [ ] Resources labeled; regional/multi-regional choices deliberate for production.
