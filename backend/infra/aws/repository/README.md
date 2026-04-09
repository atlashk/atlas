# Repository

Creates private **Amazon ECR** repositories for every Atlas application service.

| Service | Repository name |
|---|---|
| API Gateway | `atlas/api-gateway` |
| Authorization Server | `atlas/authorization-server` |
| User Service | `atlas/user-service` |
| Catalog Service | `atlas/catalog-service` |
| Inventory Service | `atlas/inventory-service` |
| Order Service | `atlas/order-service` |
| Payment Service | `atlas/payment-service` |

The namespace prefix (`atlas`) is read from the `project_name` variable and can be overridden in `terraform.tfvars`.

---

## Prerequisites

1. **Bootstrap module** must be applied first — it creates the S3 bucket used as the Terraform backend.
2. AWS credentials configured locally (`aws configure` or environment variables).

---

## Usage

```bash
cd docker-registry/

# 1. Copy and fill in your variables
cp terraform.tfvars.example terraform.tfvars

# 2. Initialise with the remote S3 backend
terraform init \
  -backend-config="bucket=<project>-terraform-state" \
  -backend-config="key=atlas/docker-registry/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="use_lockfile=true" \
  -backend-config="encrypt=true"

# 3. Preview changes
terraform plan

# 4. Apply
terraform apply
```

---

## Pushing an image

After `terraform apply`, authenticate Docker with ECR and push:

```bash
# Authenticate (replace 123456789012 and us-east-1 with real values)
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin \
      123456789012.dkr.ecr.us-east-1.amazonaws.com

# Tag and push
docker tag my-image:latest \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/atlas/api-gateway:latest

docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/atlas/api-gateway:latest
```

The exact URLs are printed by `terraform output repository_urls` after apply.

---

## Inputs

| Name | Description | Default |
|---|---|---|
| `aws_region` | AWS region | `us-east-1` |
| `project_name` | Repository namespace prefix | `atlas` |
| `environment` | dev / staging / production | `dev` |
| `services` | List of service names | see variables.tf |
| `image_tag_mutability` | MUTABLE or IMMUTABLE | `MUTABLE` |
| `scan_on_push` | Enable vulnerability scanning | `true` |
| `untagged_image_expiry_days` | Days before untagged images expire (0 = off) | `14` |
| `keep_last_n_tagged_images` | Tagged images to retain per repo (0 = all) | `10` |

## Outputs

| Name | Description |
|---|---|
| `repository_urls` | Map of service → ECR repository URL |
| `repository_arns` | Map of service → ECR repository ARN |
| `registry_id` | AWS account ID owning the registry |
