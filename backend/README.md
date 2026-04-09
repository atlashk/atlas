# Atlas — EKS Deployment

## Prerequisites

Install: 
- [Docker](https://docs.docker.com/get-docker/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Helm](https://helm.sh/docs/intro/install/)
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
- [Terraform](https://developer.hashicorp.com/terraform/install)
- [Make](https://www.gnu.org/software/make/)

> **Windows:** Run commands inside **Git Bash** or **WSL2**.

---

## AWS Infrastructure

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

Go to `backend/deployment` folder.

## 1. Configure AWS credentials

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

You can also run each step separately.

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

---

## Troubleshooting

1. Bootstrap error

```
│ Error: creating S3 Bucket (atlas-terraform-state): operation error S3: CreateBucket, https response error StatusCode: 400, RequestID: R329TN4CZ5R4P5G8, HostID: LXmXFsQegzyzfxY5qycqxt9Q8jA3l54cCiDLZliYyJzTRjZ35XD+yMoAG0IHOX8voE6iZ73Q+pk=, api error AuthorizationHeaderMalformed: The authorization header is malformed; the region 'us-east-1' is wrong; expecting 'us-west-2'
│
│   with aws_s3_bucket.terraform_state,
│   on main.tf line 17, in resource "aws_s3_bucket" "terraform_state":
│   17: resource "aws_s3_bucket" "terraform_state" {
│
╵
```


Solution: Uncomment and set value to `state_bucket_suffix` in `terraform.tfvars`.
