# Terraform — Infrastructure as Code

## Why IaC

Infrastructure described in versioned, reviewable, reproducible files. No more "the server Bob configured in 2021 and nobody dares touch."

## Core workflow

```bash
terraform init      # download providers, set up backend
terraform plan      # preview changes (ALWAYS read it)
terraform apply     # execute
terraform destroy   # tear down (careful!)
```

## Minimal example (AWS S3 + versioning)

```hcl
terraform {
  required_version = ">= 1.7"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
  backend "s3" {                     # remote state — never local in a team
    bucket         = "acme-tfstate"
    key            = "prod/network.tfstate"
    region         = "eu-west-1"
    dynamodb_table = "tf-lock"       # state locking
    encrypt        = true
  }
}

resource "aws_s3_bucket" "artifacts" {
  bucket = "acme-artifacts"
  tags   = { env = "prod", managed_by = "terraform" }
}

resource "aws_s3_bucket_versioning" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id
  versioning_configuration { status = "Enabled" }
}
```

## Golden rules

1. **Remote state + locking** — local state in a team is data loss waiting to happen.
2. **State contains secrets in clear text** — encrypt the backend, restrict access hard.
3. `plan` in CI on every PR; `apply` only from CI, never laptops.
4. Small, composable **modules**; one state per environment/domain (blast-radius control).
5. Pin provider versions; upgrade deliberately.
6. Never edit infrastructure by hand ("ClickOps") — drift kills trust; run `terraform plan` to detect it.

## Security scanning for IaC

```bash
trivy config .          # misconfigs in .tf files
tflint                  # linting + provider-specific checks
checkov -d .            # policy-as-code checks
```
Typical catches: public S3 buckets, security groups open to 0.0.0.0/0, unencrypted disks, missing logging.

## Ansible vs Terraform (quick take)

- **Terraform**: *provisioning* — create the servers, networks, managed services (declarative, stateful).
- **Ansible**: *configuration* — install packages, edit configs inside the machines (procedural, agentless).
- They compose well: Terraform creates the VM → Ansible configures it. Containers reduce the Ansible part.
