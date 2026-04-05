# Terraform — EKS Cluster for Atlas Microservices

This script provisions an **Amazon EKS cluster** on AWS to run all microservices in the Atlas project.

---

## Architecture overview

```
AWS VPC (10.0.0.0/16)
├── Public Subnet  × N AZ  → ALB (Ingress Load Balancer)
└── Private Subnet × N AZ  → EKS Node Groups
                                ├── system         (t3.medium)  — Kubernetes add-ons
                                ├── application    (t3.large)   — Microservices
                                └── infrastructure (t3.large)   — MySQL, Redis, Kafka, ES, MinIO...
```

The following EKS **Managed Add-ons** are pre-installed:
- **CoreDNS** — in-cluster DNS for service name resolution
- **kube-proxy** — internal network load balancing
- **VPC CNI** — Pod networking
- **EBS CSI Driver** — Persistent Volumes for stateful services

---

## Prerequisites

| Tool | Minimum version | Install |
|---|---|---|
| [Terraform](https://developer.hashicorp.com/terraform/install) | 1.9+ | `winget install HashiCorp.Terraform` |
| [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) | 2.x | `winget install Amazon.AWSCLI` |
| [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/) | 1.29+ | `winget install Kubernetes.kubectl` |

Verify the installations:

```bash
terraform version
aws --version
kubectl version --client
```

---

## Step 1 — Configure AWS credentials

Terraform needs access to your AWS account. The simplest approach is to use the AWS CLI:

```bash
aws configure
```

Enter the following when prompted:
- **AWS Access Key ID** — found in AWS Console → IAM → Users → Security credentials
- **AWS Secret Access Key** — obtained at the same time as the Access Key
- **Default region name** — `ap-southeast-1` (Singapore)
- **Default output format** — `json`

> **Security note:** Never commit your Access Key to git. In production environments, use IAM Roles instead of Access Keys.

Verify the credentials are working:

```bash
aws sts get-caller-identity
```

If you see `Account`, `UserId`, and `Arn` in the output, you are good to go.

---

## Step 2 — Create terraform.tfvars

```bash
cd backend/scripts/terraform/eks

# Windows (PowerShell)
Copy-Item terraform.tfvars.example terraform.tfvars

# Linux / macOS
cp terraform.tfvars.example terraform.tfvars
```

Open `terraform.tfvars` and adjust values as needed. The defaults are already configured for a **dev** environment in the **Singapore (ap-southeast-1)** region.

---

## Step 3 — Initialize Terraform

This downloads all required modules and providers. Only needs to be run once:

```bash
terraform init
```

You should see the following at the end of the output:

```
Terraform has been successfully initialized!
```

---

## Step 4 — Preview changes (plan)

See what Terraform will create before actually creating anything:

```bash
terraform plan
```

Review the output to check which resources will be created. Expect around 50–70 resources total.

Save the plan to a file for use in the next step (recommended):

```bash
terraform plan -out=atlas-eks.tfplan
```

---

## Step 5 — Apply (provision the infrastructure)

> **Cost warning:** This step will incur **real AWS charges**. Estimated cost for the default configuration (dev, Singapore):
> - 1× t3.medium + 2× t3.large ≈ **~$0.24/hour**
> - NAT Gateway ≈ **~$0.06/hour**
>
> Remember to **destroy** the cluster when you are done to avoid ongoing charges.

```bash
# If you saved a plan file
terraform apply atlas-eks.tfplan

# Or plan + apply in one step (Terraform will prompt for confirmation)
terraform apply
```

Type `yes` when prompted. Cluster creation takes approximately **10–15 minutes**.

Once complete, you will see output similar to:

```
cluster_name      = "atlas-dev"
cluster_endpoint  = "https://XXXX.gr7.ap-southeast-1.eks.amazonaws.com"
configure_kubectl = "aws eks update-kubeconfig --region ap-southeast-1 --name atlas-dev"
```

---

## Step 6 — Connect kubectl to the cluster

Copy the command from the `configure_kubectl` output and run it:

```bash
aws eks update-kubeconfig --region ap-southeast-1 --name atlas-dev
```

Verify the connection:

```bash
kubectl get nodes
```

You should see a list of nodes (approximately 7 nodes with the default configuration):

```
NAME                                           STATUS   ROLES    AGE   VERSION
ip-10-0-xxx.ap-southeast-1.compute.internal   Ready    <none>   5m    v1.31.x
...
```

---

## Destroy the cluster (when no longer needed)

> **Warning:** This command permanently deletes **all** provisioned infrastructure and **cannot be undone**.
> Make sure to back up any important data before running it.

```bash
terraform destroy
```

Type `yes` to confirm. The teardown process takes approximately 10–15 minutes.

---

## File structure

```
eks/
├── versions.tf              # Providers and S3 backend (remote state)
├── variables.tf             # All configurable input variables
├── locals.tf                # Internal locals derived from variables
├── main.tf                  # Core resources: VPC, EKS, IAM
├── outputs.tf               # Values printed after apply
├── terraform.tfvars.example # Configuration template
├── terraform.tfvars         # Your actual config values (do NOT commit)
└── .gitignore
```

---

## Remote State configuration (recommended for teams)

When working in a team, store Terraform state in S3 instead of locally. Create the S3 bucket and DynamoDB table below, then uncomment the `backend "s3"` block in [versions.tf](versions.tf).

```bash
# Create the S3 bucket for state storage
aws s3api create-bucket \
  --bucket atlas-terraform-state \
  --region ap-southeast-1 \
  --create-bucket-configuration LocationConstraint=ap-southeast-1

# Enable versioning (allows rollback if state is corrupted)
aws s3api put-bucket-versioning \
  --bucket atlas-terraform-state \
  --versioning-configuration Status=Enabled

# Create a DynamoDB table for state locking (prevents concurrent applies)
aws dynamodb create-table \
  --table-name atlas-terraform-state-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region ap-southeast-1
```

After creating these resources, uncomment the `backend "s3"` block in `versions.tf` and run `terraform init` again.

---

## Troubleshooting

| Error | Cause | Fix |
|---|---|---|
| `Error: No valid credential sources found` | AWS credentials not configured | Run `aws configure` |
| `Error: creating EKS Cluster: InvalidParameterException` | Cluster name already exists | Change `project_name` or `environment` in tfvars |
| `kubectl: Unable to connect to the server` | kubeconfig not updated | Run the command from the `configure_kubectl` output |
| `Error: creating EC2 VPC: VpcLimitExceeded` | VPC limit reached (default: 5 per region) | Delete an existing VPC or request a limit increase via AWS Support |
| Node stuck in `NotReady` | VPC CNI not fully initialized yet | Wait 2–3 minutes and check again |
