# AWS Essentials

AWS has 200+ services. You need about a dozen for most work. Here they are.

## Compute

- **EC2** — virtual machines. Full control, you manage the OS. Pricing: on-demand (flexible), reserved/savings plans (commit, save ~60%), spot (cheap, can be reclaimed).
- **Lambda** — serverless functions. No servers, pay per invocation + duration. Great for event-driven and spiky workloads; watch cold starts and the 15-min limit.
- **ECS / EKS / Fargate** — run containers. Fargate = serverless containers (no nodes to manage) → pairs with [Docker](../devsecops/containers/docker-hardening.md)/[k8s](../devsecops/containers/kubernetes-essentials.md).

## Storage

- **S3** — object storage. Cheap, durable (11 nines), infinitely scalable. Static sites, backups, data lakes, uploads. **Buckets are private by default — keep them that way** (public S3 buckets are a top breach cause).
- **EBS** — block storage (virtual disks) attached to EC2.
- **EFS** — shared network file system.

## Database

- **RDS** — managed relational (Postgres, MySQL, etc.): backups, patching, replicas handled for you.
- **Aurora** — AWS's cloud-native, faster RDS-compatible engine.
- **DynamoDB** — managed NoSQL key-value, single-digit-ms at any scale. Great fit for serverless.
- **ElastiCache** — managed Redis/Memcached for caching.

## Networking & delivery

- **VPC** — your private network: subnets (public/private), route tables, security groups (instance firewalls), NAT gateways.
- **Route 53** — DNS.
- **CloudFront** — CDN, caches content at edge locations near users.
- **ELB / ALB** — load balancers spreading traffic across instances.
- **API Gateway** — managed front door for APIs (often + Lambda).

## Identity & operations

- **IAM** — users, roles, policies. **The most important service** — everything is gated by it. Rules: least privilege, roles over long-lived keys, MFA on root, never commit access keys.
- **CloudWatch** — metrics, logs, alarms → [observability](../devsecops/monitoring/observability.md).
- **CloudFormation / CDK** — infrastructure as code (or use [Terraform](../devsecops/iac/terraform-basics.md)).

## A typical web app on AWS

```
Route 53 (DNS)
   → CloudFront (CDN, static assets from S3)
   → ALB (load balancer)
      → ECS/Fargate or EC2 (app, private subnet, multi-AZ)
         → RDS (database, private subnet, multi-AZ)
         → ElastiCache (Redis)
   IAM roles gate every service · CloudWatch watches it all
```

## Starter safety checklist

- [ ] MFA on the root account; then **never use root** — create IAM users/roles.
- [ ] No access keys in code — use IAM roles; scan repos for leaked keys.
- [ ] S3 buckets private unless deliberately public; block-public-access on.
- [ ] Billing alarm + budget set before you deploy anything.
- [ ] Resources tagged; multi-AZ for anything production.
