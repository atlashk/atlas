# Atlas — EKS Deployment

## Prerequisites

Install: [Make](https://www.gnu.org/software/make/), [Terraform](https://developer.hashicorp.com/terraform/install), [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html), [Docker](https://docs.docker.com/get-docker/), [kubectl](https://kubernetes.io/docs/tasks/tools/), [Helm](https://helm.sh/docs/intro/install/)

> **Windows:** Run commands inside **Git Bash** or **WSL2**.

---

## 1. Configure AWS credentials

```bash
aws configure
```

---

## 2. Configure variables

```bash
cp terraform/aws/bootstrap/terraform.tfvars.example  terraform/aws/bootstrap/terraform.tfvars
cp terraform/aws/cluster/terraform.tfvars.example    terraform/aws/cluster/terraform.tfvars
cp terraform/aws/repository/terraform.tfvars.example terraform/aws/repository/terraform.tfvars
```

Edit each `terraform.tfvars` and fill in `aws_region`, `project_name`, etc.

> `terraform.tfvars` files are git-ignored — **do not commit them**.

---

## 3. Deploy

```bash
cd backend/deployment
make install
```

That's it. The command runs all 5 steps automatically:

| Step | What it does | Duration |
|------|-------------|----------|
| `bootstrap` | Creates S3 bucket for Terraform state | ~1 min |
| `cluster` | Creates VPC + EKS cluster | ~15 min |
| `repository` | Creates ECR image repositories | ~1 min |
| `push` | Builds & pushes Docker images | ~5–10 min |
| `helm` | Deploys all services to Kubernetes | ~5 min |

---

## 4. Connect kubectl

```bash
aws eks update-kubeconfig --region us-east-1 --name atlas-dev
kubectl get pods -n atlas
```

---

## 5. Tear down

```bash
make uninstall
```

---

## Useful commands

```bash
make help           # list all targets
make check-prereqs  # verify tools are installed

# Deploy to a custom namespace
make install HELM_RELEASE_NAME=atlas-prod HELM_NAMESPACE=atlas-prod
```

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
├── Makefile                         # Automated installer — all 5 steps via make targets
│
├── terraform/
│   └── aws/
│       ├── bootstrap/               # Step 1: Create S3 bucket for Terraform remote state
│       │   ├── main.tf
│       │   ├── variables.tf
│       │   ├── outputs.tf
│       │   ├── locals.tf
│       │   ├── versions.tf
│       │   └── terraform.tfvars.example
│       │
│       ├── cluster/                 # Step 2: Create VPC + EKS cluster + IAM roles
│       │   ├── main.tf              #   — VPC, EKS, node groups, IRSA, LB Controller
│       │   ├── variables.tf
│       │   ├── outputs.tf
│       │   ├── locals.tf
│       │   ├── versions.tf
│       │   ├── terraform.tfvars.example
│       │   └── .gitignore           #   — terraform.tfvars is excluded from git
│       │
│       └── repository/              # Step 3: Create ECR image repositories
│           ├── main.tf
│           ├── variables.tf
│           ├── outputs.tf
│           ├── locals.tf
│           ├── versions.tf
│           └── terraform.tfvars.example
│
└── helm/                            # Step 5: Deploy all Kubernetes resources
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

| Tool | Minimum version | Install (macOS/Linux) | Install (Windows) |
|---|---|---|---|
| [GNU Make](https://www.gnu.org/software/make/) | 3.81+ | pre-installed / `brew install make` | `winget install GnuWin32.Make` |
| [Terraform](https://developer.hashicorp.com/terraform/install) | 1.9+ | `brew install terraform` | `winget install HashiCorp.Terraform` |
| [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) | 2.x | `brew install awscli` | `winget install Amazon.AWSCLI` |
| [Docker](https://docs.docker.com/get-docker/) | 24.x+ | `brew install --cask docker` | `winget install Docker.DockerDesktop` |
| [kubectl](https://kubernetes.io/docs/tasks/tools/) | 1.29+ | `brew install kubectl` | `winget install Kubernetes.kubectl` |
| [Helm](https://helm.sh/docs/intro/install/) | 3.x | `brew install helm` | `winget install Helm.Helm` |
| [Python](https://www.python.org/downloads/) | 3.x | pre-installed | `winget install Python.Python.3` |

> **Windows users:** The `Makefile` requires a bash-compatible shell. Install [Git for Windows](https://git-scm.com/download/win) (which includes Git Bash) and ensure `bash` is on your `PATH`, or run all `make` commands inside **WSL2**.

Verify the installations:

```bash
make --version
terraform version
aws --version
docker version
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
cp terraform/aws/bootstrap/terraform.tfvars.example terraform/aws/bootstrap/terraform.tfvars
```

Edit `terraform/aws/bootstrap/terraform.tfvars`:

```hcl
aws_region          = "us-east-1"
project_name        = "atlas"
state_bucket_suffix = ""   # add a suffix if the bucket name is taken (S3 names must be globally unique)
```

### 5.2. EKS Cluster

```bash
cp terraform/aws/cluster/terraform.tfvars.example terraform/aws/cluster/terraform.tfvars
```

Key variables in `terraform/aws/cluster/terraform.tfvars`:

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

### 5.3. ECR Repositories

```bash
cp terraform/aws/repository/terraform.tfvars.example terraform/aws/repository/terraform.tfvars
```

Edit `terraform/aws/repository/terraform.tfvars`:

```hcl
aws_region   = "us-east-1"
project_name = "atlas"

# List of services — each gets its own ECR repository (<project_name>/<service>)
services = [
  "api-gateway",
  "authorization-server",
  "catalog-service",
  "inventory-service",
  "order-service",
  "payment-service",
  "user-service",
]
```

> **Note:** All `terraform.tfvars` files are listed in `.gitignore`. **Do not commit** them.

---

## 6. Automated Deployment via Makefile (Recommended)

The `Makefile` orchestrates the full deployment in **5 sequential steps**. 

| Step | Target | Duration | Description |
|------|--------|----------|-------------|
| 1 | `bootstrap` | ~1 min | Creates S3 bucket for Terraform remote state |
| 2 | `cluster` | ~15 min | Creates VPC + EKS cluster + IAM roles |
| 3 | `repository` | ~1 min | Creates ECR image repositories |
| 4 | `push` | ~5–10 min | Builds Docker images and pushes to ECR |
| 5 | `helm` | ~5 min | Deploys all Kubernetes resources via Helm |

Run `make help` at any time to see all available targets and variables.

```bash
cd backend/deployment
make help
```

```
Atlas — Makefile for AWS

Install targets
  make install              Full 5-step install (bootstrap → cluster → repository → push → helm)
  make bootstrap            Step 1: Terraform bootstrap (S3 remote-state bucket)
  make cluster              Step 2: Terraform for EKS cluster creation
  make repository           Step 3: Terraform for ECR image repositories creation
  make push                 Step 4: Build Docker images and push to ECR
  make helm                 Step 5: Helm install / upgrade

Uninstall targets
  make uninstall            Full 4-step uninstall (helm → repository → cluster → bootstrap)
  make destroy-helm         Step 1: Helm uninstall
  make destroy-repository   Step 2: Destroy ECR repositories
  make destroy-cluster      Step 3: Destroy EKS cluster
  make destroy-bootstrap    Step 4: Destroy S3 remote-state bucket

Variables (override on command line)
  HELM_RELEASE_NAME   Helm release name    (default: atlas)
  HELM_NAMESPACE      Kubernetes namespace  (default: atlas)
```

Check prerequisites:

```bash
make check-prereqs
```

Run all steps:

```bash
make install
```

Override Makefile variables:

```bash
# Deploy to a different namespace / release name
make helm HELM_RELEASE_NAME=atlas-staging HELM_NAMESPACE=atlas-staging

# Run the full install with a custom release name
make install HELM_RELEASE_NAME=atlas-prod HELM_NAMESPACE=atlas-prod
```

---

## 7. Manual Deployment (Step by Step)

This section walks through each step manually — useful for understanding the internals or debugging individual stages.

### Step 1 — Bootstrap: Create Terraform remote state backend

```bash
cd terraform/aws/bootstrap

terraform init
terraform apply
```

Note the S3 bucket name from the output:

```
state_bucket_name   = "atlas-terraform-state-xxxx"
state_bucket_region = "us-east-1"
```

### Step 2 — EKS Cluster

```bash
cd terraform/aws/cluster

terraform init -migrate-state -force-copy \
  -backend-config="bucket=atlas-terraform-state-xxxx" \
  -backend-config="key=atlas/eks/terraform.tfstate" \
  -backend-config="region=us-east-1"

terraform plan
terraform apply   # ~10–15 minutes
```

Output after apply:

```
cluster_name      = "atlas-dev"
cluster_endpoint  = "https://XXXX.gr7.us-east-1.eks.amazonaws.com"
configure_kubectl = "aws eks update-kubeconfig --region us-east-1 --name atlas-dev"
```

### Step 3 — ECR Repositories

```bash
cd terraform/aws/repository

terraform init -migrate-state -force-copy \
  -backend-config="bucket=atlas-terraform-state-xxxx" \
  -backend-config="key=atlas/ecr/terraform.tfstate" \
  -backend-config="region=us-east-1"

terraform apply
```

### Step 4 — Build & Push Docker images

```bash
# Login to ECR
REGISTRY_ID=$(terraform -chdir=terraform/aws/repository output -raw registry_id)
REGION=us-east-1

aws ecr get-login-password --region $REGION \
  | docker login --username AWS --password-stdin \
      "$REGISTRY_ID.dkr.ecr.$REGION.amazonaws.com"

# Build and push each service
REPO_URL=$(terraform -chdir=terraform/aws/repository output -json repository_urls \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['api-gateway'])")
IMAGE_TAG=$(git rev-parse --short HEAD)

docker build --platform linux/amd64 \
  -t "$REPO_URL:$IMAGE_TAG" -t "$REPO_URL:latest" \
  ../services/api-gateway

docker push "$REPO_URL:$IMAGE_TAG"
docker push "$REPO_URL:latest"

# Repeat for each service...
```

### Step 5 — Configure kubectl

```bash
aws eks update-kubeconfig --region us-east-1 --name atlas-dev
```

### Step 6 — Helm install

```bash
helm upgrade --install atlas helm/ \
  --namespace atlas \
  --create-namespace \
  --values helm/values.yaml \
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

### Via Makefile (Recommended)

```bash
make uninstall
```

This runs 4 steps in reverse order:

| Step | Target | Description |
|------|--------|-------------|
| 1 | `destroy-helm` | Helm uninstall + delete namespace |
| 2 | `destroy-repository` | Destroy ECR repositories |
| 3 | `destroy-cluster` | Destroy EKS cluster + VPC (~10–15 min) |
| 4 | `destroy-bootstrap` | Empty + destroy S3 remote-state bucket |

You can also destroy individual steps:

```bash
make destroy-helm         # uninstall Helm release only
make destroy-repository   # destroy ECR repositories only
make destroy-cluster      # destroy EKS cluster only
make destroy-bootstrap    # destroy S3 bucket only (run last)
```

### Via Terraform/Helm manually

```bash
# Step 1: Uninstall the Helm release (releases the ALB and PVCs first)
helm uninstall atlas -n atlas

# Step 2: Destroy ECR repositories
cd terraform/aws/repository
terraform destroy

# Step 3: Destroy the EKS cluster and VPC (~10–15 minutes)
cd terraform/aws/cluster
terraform destroy

# Step 4 (optional): Destroy the S3 remote-state bucket
cd terraform/aws/bootstrap
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
