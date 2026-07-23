# Azure Essentials

Same pattern as [AWS essentials](aws-essentials.md): a dozen services cover most real work, in Microsoft's naming and organizational model (Azure groups everything by **Resource Group**, which is the one organizational habit that trips up AWS people first).

## Compute

- **Virtual Machines** — the EC2 equivalent. Same pricing shapes: pay-as-you-go, reserved instances, spot VMs.
- **Azure Functions** — serverless, the Lambda equivalent. Consumption plan (pay per execution) or Premium (pre-warmed, no cold starts, costs more).
- **AKS (Azure Kubernetes Service)** — managed Kubernetes control plane, free (you pay only for the worker nodes) — genuinely one of Azure's stronger cost stories vs the other two clouds.
- **Container Apps** — serverless containers, Azure's answer to Fargate/Cloud Run; scales to zero, good for APIs and background workers without managing a cluster.

## Storage

- **Blob Storage** — the S3 equivalent. Hot/cool/archive tiers for cost optimization based on access frequency.
- **Azure Files** — managed SMB/NFS file shares (the EFS equivalent).
- **Managed Disks** — the EBS equivalent, attached to VMs.

## Database

- **Azure SQL Database** — managed SQL Server, serverless tier available (scales to near-zero cost when idle).
- **Azure Database for PostgreSQL / MySQL** — managed open-source engines, same idea as RDS.
- **Cosmos DB** — Azure's standout: **multi-model** (document, key-value, graph, wide-column via different APIs) with global multi-region writes and tunable consistency — genuinely more flexible than a single-model NoSQL offering.
- **Azure Cache for Redis** — managed Redis.

## Networking

- **Virtual Network (VNet)** — the VPC equivalent: subnets, network security groups (the security-group equivalent), route tables.
- **Azure DNS** — DNS hosting.
- **Azure Front Door / CDN** — the CloudFront equivalent, global edge caching + routing.
- **Application Gateway / Load Balancer** — L7/L4 load balancing.
- **API Management** — managed API gateway with policies, throttling, developer portal.

## Identity & operations

- **Microsoft Entra ID** (formerly Azure AD) — identity and access, the IAM equivalent, and also the enterprise SSO/directory service most organizations already run — Azure's biggest structural advantage if a company is already a Microsoft shop.
- **Azure Monitor / Log Analytics** — metrics, logs, alerts → [observability](../devsecops/monitoring/observability.md).
- **Bicep / ARM templates** — infrastructure as code (Bicep is the modern, cleaner successor to raw ARM JSON), or use [Terraform](../devsecops/iac/terraform-basics.md) which supports Azure equally well.

## Resource Groups — the organizational habit to learn first

```
Subscription
  └── Resource Group "notes-app-prod"
         ├── App Service / Container App
         ├── Azure SQL Database
         ├── VNet
         └── Storage Account
```
Everything related to one app/environment lives in one Resource Group — delete the group, everything in it goes with it. This is genuinely convenient for cleanup and cost tracking, and the first mental model shift coming from AWS's flatter, tag-based organization.

## A typical web app on Azure

```
Azure DNS
   → Front Door (CDN + global routing)
   → Application Gateway (load balancer, WAF)
      → Container Apps or AKS (app, private VNet)
         → Azure SQL Database (private endpoint)
         → Azure Cache for Redis
   Entra ID gates every service · Azure Monitor watches it all
```

## Starter safety checklist

- [ ] MFA enforced on all admin accounts via Entra ID Conditional Access.
- [ ] No connection strings/keys in code — use **Managed Identity** (Azure's equivalent of IAM roles, lets a service authenticate to another Azure service with zero stored credentials).
- [ ] Storage accounts: public access disabled unless deliberate.
- [ ] Budget + cost alerts configured before deploying anything real.
- [ ] Resources tagged and grouped by environment; zone-redundant for production.
