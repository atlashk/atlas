# ==============================================================
# Terraform Backend Bootstrap — README
# ==============================================================

## What this module does

Creates two AWS resources that act as the **remote state backend** for all
other Terraform modules in this repository:

| Resource | Name (default) | Purpose |
|---|---|---|
| **S3 Bucket** | `atlas-terraform-state` | Stores `.tfstate` files remotely & safely |
| **DynamoDB Table** | `atlas-terraform-state-lock` | Provides state locking to prevent concurrent runs |

---

## Prerequisites

- AWS CLI configured (`aws configure`) or environment variables set:
  ```
  AWS_ACCESS_KEY_ID
  AWS_SECRET_ACCESS_KEY
  AWS_DEFAULT_REGION   (optional, overridden by var.aws_region)
  ```
- Terraform ≥ 1.9 installed
- The IAM user / role must have permissions to create S3 buckets and DynamoDB tables.

---

## Usage

### 1. Bootstrap the backend (run once)

```bash
cd deployment/terraform/eks/init

cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars as needed

terraform init
terraform plan
terraform apply
```

After `apply` succeeds, Terraform will print the `backend_config_snippet` output —
copy it and paste it into the relevant module's `versions.tf`.

### 2. Enable remote state in `eks/cluster`

Open `deployment/terraform/eks/cluster/versions.tf` and **uncomment** the
`backend "s3"` block, then fill in the values from the output above:

```hcl
backend "s3" {
  bucket         = "atlas-terraform-state"
  key            = "atlas/eks/terraform.tfstate"
  region         = "us-east-1"
  dynamodb_table = "atlas-terraform-state-lock"
  encrypt        = true
}
```

Then migrate the local state (if any) to S3:

```bash
cd deployment/terraform/eks/cluster
terraform init -migrate-state
```

---

## Resource details

### S3 Bucket

| Feature | Setting |
|---|---|
| Versioning | **Enabled** — allows rolling back to a previous state |
| Encryption | **AES-256** (SSE-S3) |
| Public access | **Fully blocked** |
| Lifecycle | Non-current versions older than 90 days are deleted (keeps last 10) |
| `prevent_destroy` | **true** — Terraform refuses to delete this bucket |

### DynamoDB Table

| Feature | Setting |
|---|---|
| Billing mode | `PAY_PER_REQUEST` (no provisioned capacity cost) |
| Hash key | `LockID` (required by Terraform) |
| Point-in-time recovery | **Enabled** |
| `prevent_destroy` | **true** |

---

## Destroying the bootstrap resources

Because both resources have `lifecycle { prevent_destroy = true }`, you must
**comment out** those blocks before running `terraform destroy`.

> ⚠️ Only destroy this module after all other modules have been fully destroyed
> and their state files have been removed from S3.
