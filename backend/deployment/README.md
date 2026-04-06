# Atlas — EKS Deployment Guide

End-to-end guide for deploying the **Atlas Microservices** stack on **Amazon EKS** using **Terraform** (infrastructure) and **Helm** (Kubernetes resources).

---

## Table of Contents

1. [Architecture](#1-architecture)
2. [Directory Structure](#2-directory-structure)
3. [Prerequisites](#3-prerequisites)
4. [Configure AWS Credentials](#4-configure-aws-credentials)
5. [Configure Variables](#5-configure-variables)
6. [Automated Deployment (Recommended)](#6-automated-deployment-recommended)
7. [Manual Deployment (Step by Step)](#7-manual-deployment-step-by-step)
8. [Connect kubectl](#8-connect-kubectl)
9. [Verify the Deployment](#9-verify-the-deployment)
10. [Tear Down](#10-tear-down)

---

## 1. Architecture

### AWS Infrastructure

```
AWS VPC (10.0.0.0/16)
│
├── Public Subnets  × N AZ
│   └── Application Load Balancer (ALB)
│       └── Receives Internet traffic → forwards to the cluster
│
└── Private Subnets × N AZ
    └── EKS Managed Node Groups
        ├── system  (t3.medium)  — Kubernetes add-ons (CoreDNS, metrics-server...)
        ├── app     (t3.large)   — Microservices (API Gateway, business services)
        └── infra   (t3.large)   — Stateful services (MySQL, Redis, Kafka, MinIO...)
```

A **NAT Gateway** allows nodes in the private subnets to reach the Internet (e.g. pulling Docker images) without a public IP address.

### EKS Managed Add-ons

| Add-on | Purpose |
|---|---|
| **CoreDNS** | In-cluster DNS — resolves service names |
| **kube-proxy** | Internal load balancing for Services |
| **VPC CNI** | Pod networking (each Pod gets a VPC IP) |
| **EBS CSI Driver** | Persistent Volumes for stateful services (MySQL, ES, MinIO...) |

### Microservices (Node Group: app)

| Service | Description |
|---|---|
| **api-gateway** | Single entry point — routes requests to downstream services |
| **authorization-server** | OAuth2 / OIDC authorization server |
| **catalog-service** | Product catalog management |
| **inventory-service** | Inventory management |
| **order-service** | Order management |
| **payment-service** | Payment processing |
| **user-service** | User management |

### Infrastructure Services (Node Group: infra)

| Service | Description |
|---|---|
| **MySQL** | Relational database |
| **PostgreSQL** | Relational database |
| **Redis** | Cache / KV store |
| **Kafka** | Message broker |
| **Elasticsearch** | Full-text search |
| **MinIO** | Object storage |
| **Keycloak** | Identity & Access Management |
| **RabbitMQ** | Message broker |
| **Qdrant** | Vector database |
| **Grafana + Prometheus** | Monitoring & Alerting |
| **Loki + Promtail** | Log aggregation |
| **Tempo** | Distributed tracing |
| **OpenTelemetry Collector** | Telemetry pipeline |

### IAM Roles for Service Accounts (IRSA)

Instead of attaching policies to the node IAM Role, IRSA grants least-privilege AWS permissions per workload:

| IRSA Role | Used by |
|---|---|
| `atlas-{env}-ebs-csi-driver` | EBS CSI Driver — create/delete EBS volumes |
| `atlas-{env}-aws-load-balancer-controller` | AWS LB Controller — create/manage ALBs |

---

## 2. Directory Structure

```
deployment/
├── install.eks.sh                   # Automated installer script (all 3 steps)
│
├── terraform/
│   └── eks/
│       ├── bootstrap/               # Step 1: Create S3 bucket + DynamoDB for Terraform remote state
│       │   ├── main.tf
│       │   ├── variables.tf
│       │   ├── outputs.tf
│       │   ├── locals.tf
│       │   ├── versions.tf
│       │   └── terraform.tfvars.example
│       │
│       └── cluster/                 # Step 2: Create VPC + EKS cluster + IAM roles
│           ├── main.tf              #   — VPC, EKS, node groups, IRSA, LB Controller
│           ├── variables.tf
│           ├── outputs.tf
│           ├── locals.tf
│           ├── versions.tf          #   — Provider config + S3 backend block (commented out)
│           ├── terraform.tfvars.example
│           └── .gitignore           #   — terraform.tfvars is excluded from git
│
└── helm/                            # Step 3: Deploy all Kubernetes resources
    ├── Chart.yaml
    ├── values.yaml                  # Default Helm values
    ├── templates/
    │   ├── _helpers.tpl
    │   ├── app/                     # Deployment + Service + Ingress for microservices
    │   ├── infra/                   # StatefulSet + Service for infrastructure
    │   └── common/                  # RBAC resources
    └── files/                       # ConfigMap files (Grafana dashboards, Keycloak realm...)
```

---

## 3. Prerequisites

| Tool | Minimum version | Install (Windows) |
|---|---|---|
| [Terraform](https://developer.hashicorp.com/terraform/install) | 1.9+ | `winget install HashiCorp.Terraform` |
| [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) | 2.x | `winget install Amazon.AWSCLI` |
| [kubectl](https://kubernetes.io/docs/tasks/tools/) | 1.29+ | `winget install Kubernetes.kubectl` |
| [Helm](https://helm.sh/docs/intro/install/) | 3.x | `winget install Helm.Helm` |

Verify the installations:

```bash
terraform version
aws --version
kubectl version --client
helm version
```

---

## 4. Configure AWS Credentials

```bash
aws configure
```

Enter the following values when prompted:

| Field | Value |
|---|---|
| AWS Access Key ID | AWS Console → IAM → Users → Security credentials |
| AWS Secret Access Key | Obtained at the same time as the Access Key |
| Default region name | `us-east-1` |
| Default output format | `json` |

Verify the credentials:

```bash
aws sts get-caller-identity
```

Expected output:
```json
{
    "UserId": "AIDA...",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/alice"
}
```

> **Security:** Never commit Access Keys to git. In production environments, use IAM Roles instead of static Access Keys.

---

## 5. Configure Variables

### 5.1. Bootstrap (Terraform remote state)

```bash
cd terraform/eks/bootstrap
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` if you need to change the bucket name:

```hcl
aws_region          = "us-east-1"
project_name        = "atlas"
state_bucket_suffix = ""   # add a suffix if the bucket name is taken (S3 names must be globally unique)
```

### 5.2. EKS Cluster

```bash
cd terraform/eks/cluster
cp terraform.tfvars.example terraform.tfvars
```

Key variables in `terraform.tfvars`:

```hcl
# AWS region
aws_region = "us-east-1"

# Project name — used as a prefix for all AWS resource names
project_name = "atlas"

# Environment: "dev" | "staging" | "production"
environment = "dev"

# Kubernetes version
kubernetes_version = "1.31"

# Number of Availability Zones (dev: 2 AZs to reduce cost, production: 3 AZs)
availability_zones_count = 2

# VPC CIDR
vpc_cidr = "10.0.0.0/16"

# Node Group: system — runs Kubernetes add-ons
system_node_group = {
  instance_types = ["t3.medium"]
  min_size       = 1
  max_size       = 2
  desired_size   = 1
  disk_size_gb   = 20
}

# Node Group: app — runs microservices
app_node_group = {
  instance_types = ["t3.large"]
  min_size       = 1
  max_size       = 2
  desired_size   = 1
  disk_size_gb   = 30
}

# Node Group: infra — runs MySQL, Redis, Kafka, ES, MinIO...
infra_node_group = {
  instance_types = ["t3.large"]
  min_size       = 1
  max_size       = 2
  desired_size   = 1
  disk_size_gb   = 30
}

# IAM principals granted cluster-admin (optional)
# admin_iam_principals = [
#   "arn:aws:iam::123456789012:root",
#   "arn:aws:iam::123456789012:user/alice",
# ]
```

> **Note:** Both `terraform.tfvars` files are already listed in `.gitignore`. **Do not commit** them.

---

## 6. Automated Deployment (Recommended)

`install.eks.sh` runs all 3 steps in sequence:

1. **Bootstrap** — creates the S3 bucket + DynamoDB table for Terraform remote state
2. **Cluster** — creates the VPC, EKS cluster, node groups, and IAM roles
3. **Helm** — deploys all Kubernetes resources onto the cluster

**Prerequisite:** Both `terraform.tfvars` files must exist (see [Section 5](#5-configure-variables)).

```bash
cd backend/deployment
chmod +x install.eks.sh
./install.eks.sh
```

The full process takes approximately **15–25 minutes**. Progress is printed at each step.

> **Estimated cost (dev, us-east-1):**
> - 1× t3.medium + 2× t3.large ≈ **~$0.24/hour**
> - NAT Gateway ≈ **~$0.06/hour**
>
> Remember to [tear down the cluster](#10-tear-down) when it is no longer needed to avoid ongoing charges.

---

## 7. Manual Deployment (Step by Step)

### Step 1 — Bootstrap: Create Terraform remote state backend

```bash
cd terraform/eks/bootstrap

terraform init
terraform apply
```

Note the S3 bucket and DynamoDB table names from the output:

```
state_bucket_name = "atlas-terraform-state"
lock_table_name   = "atlas-terraform-state-lock"
```

### Step 2 — EKS Cluster

```bash
cd terraform/eks/cluster
```

**(Optional — recommended for teams)** Enable remote state by opening `versions.tf` and uncommenting the `backend "s3"` block, then filling in the bucket and table names from Step 1:

```hcl
backend "s3" {
  bucket         = "atlas-terraform-state"
  key            = "atlas/eks/terraform.tfstate"
  region         = "us-east-1"
  dynamodb_table = "atlas-terraform-state-lock"
  encrypt        = true
}
```

Then run:

```bash
terraform init          # if you added the S3 backend: terraform init -migrate-state
terraform plan          # preview changes
terraform apply         # provision infrastructure (~10–15 minutes)
```

Output after apply:

```
cluster_name      = "atlas-dev"
cluster_endpoint  = "https://XXXX.gr7.us-east-1.eks.amazonaws.com"
configure_kubectl = "aws eks update-kubeconfig --region us-east-1 --name atlas-dev"
```

### Step 3 — Configure kubectl

Run the command from the `configure_kubectl` output:

```bash
aws eks update-kubeconfig --region us-east-1 --name atlas-dev
```

### Step 4 — Helm install

```bash
cd helm

helm upgrade --install atlas . \
  --namespace atlas \
  --create-namespace \
  --values values.yaml \
  --wait \
  --timeout 30m
```

---

## 8. Connect kubectl

After the cluster is ready, update your kubeconfig:

```bash
aws eks update-kubeconfig --region us-east-1 --name atlas-dev
```

Verify the connection:

```bash
kubectl get nodes
```

Expected output (default dev configuration):

```
NAME                                             STATUS   ROLES    AGE   VERSION
ip-10-0-1-xxx.us-east-1.compute.internal         Ready    <none>   10m   v1.31.x
ip-10-0-2-xxx.us-east-1.compute.internal         Ready    <none>   10m   v1.31.x
ip-10-0-3-xxx.us-east-1.compute.internal         Ready    <none>   10m   v1.31.x
```

---

## 9. Verify the Deployment

```bash
# Check all Pods are running
kubectl get pods -n atlas

# Check Services
kubectl get svc -n atlas

# Get the ALB address (if Ingress is enabled)
kubectl get ingress -n atlas

# Get the ALB DNS name
kubectl get ingress -n atlas atlas-api-gateway \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

Check Node Groups:

```bash
kubectl get nodes -L role
```

Expected output:

```
NAME                              STATUS   ROLES    AGE   VERSION    ROLE
ip-10-0-1-xx...compute.internal   Ready    <none>   15m   v1.31.x    system
ip-10-0-2-xx...compute.internal   Ready    <none>   15m   v1.31.x    app
ip-10-0-3-xx...compute.internal   Ready    <none>   15m   v1.31.x    infra
```

---

## 10. Tear Down

> **Warning:** These commands permanently delete **all** provisioned infrastructure and **cannot be undone**. Back up any important data before proceeding.

```bash
# Step 1: Uninstall the Helm release (releases the ALB and PVCs first)
helm uninstall atlas -n atlas

# Step 2: Destroy the EKS cluster and VPC (~10–15 minutes)
cd terraform/eks/cluster
terraform destroy

# Step 3 (optional): Destroy the S3 bucket and DynamoDB table
# Only run this if you want a complete clean-up
cd ../bootstrap
terraform destroy
```

> **Note:** Always uninstall Helm before running `terraform destroy`. Skipping this step leaves the Load Balancer and PVCs orphaned, which causes Terraform to hang during teardown.

---

## References

- [Amazon EKS Documentation](https://docs.aws.amazon.com/eks/latest/userguide/)
- [Terraform AWS EKS Module](https://registry.terraform.io/modules/terraform-aws-modules/eks/aws/latest)
- [AWS Load Balancer Controller](https://kubernetes-sigs.github.io/aws-load-balancer-controller/)
- [EBS CSI Driver](https://github.com/kubernetes-sigs/aws-ebs-csi-driver)
- [Helm Documentation](https://helm.sh/docs/)
