# ==============================================================
# VPC — Private network on AWS
# ==============================================================
# Kubernetes worker nodes are placed in private subnets (no public
# IP) for security. A NAT Gateway allows nodes to reach the
# internet (e.g. pulling Docker images) without a public IP.
# ==============================================================

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "${local.cluster_name}-vpc"
  cidr = var.vpc_cidr

  azs = local.azs

  # Private subnets: where EKS nodes run
  # cidrsubnet("10.0.0.0/16", 4, 0) → 10.0.0.0/20  (~4094 IPs)
  # cidrsubnet("10.0.0.0/16", 4, 1) → 10.0.16.0/20 (~4094 IPs)
  private_subnets = [for k, v in local.azs : cidrsubnet(var.vpc_cidr, 4, k)]

  # Public subnets: where the ALB (Ingress Load Balancer) is placed
  # cidrsubnet("10.0.0.0/16", 8, 48) → 10.0.48.0/24 (~254 IPs)
  # cidrsubnet("10.0.0.0/16", 8, 49) → 10.0.49.0/24 (~254 IPs)
  public_subnets = [for k, v in local.azs : cidrsubnet(var.vpc_cidr, 8, k + 48)]

  # NAT Gateway: allows private subnets to reach the internet (pull images)
  enable_nat_gateway = true
  # dev: single NAT GW (cheaper)
  # production: one NAT GW per AZ (high availability)
  single_nat_gateway = var.environment != "production"

  enable_dns_hostnames = true
  enable_dns_support   = true

  # Required tags for the AWS Load Balancer Controller to discover the correct subnets
  public_subnet_tags = {
    "kubernetes.io/role/elb" = 1
  }
  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = 1
  }

  tags = local.common_tags
}

# ==============================================================
# EKS Cluster
# ==============================================================
# Uses the official terraform-aws-modules/eks module.
# The module handles: IAM roles, security groups, OIDC provider.
# ==============================================================

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = local.cluster_name
  cluster_version = var.kubernetes_version

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  # Allow public access to the API server (required to use kubectl remotely)
  # In production, restrict access via cluster_endpoint_public_access_cidrs
  cluster_endpoint_public_access = true

  # IRSA (IAM Roles for Service Accounts): allows Pods to assume IAM roles
  # The secure way for AWS components (EBS CSI, LB Controller...) to call AWS APIs
  enable_irsa = true

  # ----------------------------------------------------------
  # EKS Managed Add-ons
  # AWS manages updates and patching for these add-ons
  # ----------------------------------------------------------
  cluster_addons = {
    # In-cluster DNS (resolves service names)
    coredns = {
      most_recent = true
    }

    # Internal network load balancing for Services
    kube-proxy = {
      most_recent = true
    }

    # CNI plugin: handles Pod networking (must initialize before compute nodes)
    vpc-cni = {
      most_recent    = true
      before_compute = true
    }

    # EBS CSI Driver: enables Pods to use EBS volumes as persistent storage
    # (required for MySQL, Elasticsearch, MinIO...)
    aws-ebs-csi-driver = {
      most_recent              = true
      service_account_role_arn = module.ebs_csi_irsa.iam_role_arn
    }
  }

  # ----------------------------------------------------------
  # Managed Node Groups
  # AWS automatically handles EC2 provisioning and AMI updates
  # ----------------------------------------------------------
  eks_managed_node_groups = {

    # ── NODE GROUP: system ──────────────────────────────────
    # Runs Kubernetes add-ons: CoreDNS, metrics-server...
    # Small instances (t3.medium) — light workload
    system = {
      name           = "${local.cluster_name}-system"
      instance_types = var.system_node_group.instance_types
      min_size       = var.system_node_group.min_size
      max_size       = var.system_node_group.max_size
      desired_size   = var.system_node_group.desired_size
      disk_size      = var.system_node_group.disk_size_gb

      labels = {
        role = "system"
      }
    }

    # ── NODE GROUP: app ─────────────────────────────
    # Runs the Atlas microservices:
    #   api-gateway, catalog-service, inventory-service,
    #   order-service, payment-service, user-service,
    #   authorization-server, config-server, discovery-server
    app = {
      name           = "${local.cluster_name}-app"
      instance_types = var.app_node_group.instance_types
      min_size       = var.app_node_group.min_size
      max_size       = var.app_node_group.max_size
      desired_size   = var.app_node_group.desired_size
      disk_size      = var.app_node_group.disk_size_gb

      labels = {
        role = "app"
      }
    }

    # ── NODE GROUP: infra ──────────────────────────
    # Runs stateful services:
    #   MySQL, Redis, Kafka, Elasticsearch, MinIO,
    #   Loki, Prometheus, Tempo
    # Taint "dedicated=infra:NoSchedule" ensures only
    # Pods with a matching toleration are scheduled here
    infra = {
      name          = "${local.cluster_name}-infra"
      instance_types = var.infra_node_group.instance_types
      min_size       = var.infra_node_group.min_size
      max_size       = var.infra_node_group.max_size
      desired_size   = var.infra_node_group.desired_size
      disk_size      = var.infra_node_group.disk_size_gb

      labels = {
        role = "infra"
      }

      taints = {
        dedicated = {
          key    = "dedicated"
          value  = "infra"
          effect = "NO_SCHEDULE"
        }
      }
    }
  }

  # Automatically grant admin permissions to the IAM identity running terraform
  enable_cluster_creator_admin_permissions = true

  # ----------------------------------------------------------
  # Additional Admin Access Entries
  # ----------------------------------------------------------
  # Grant cluster-admin access to every principal listed in
  # var.admin_iam_principals (e.g. the AWS root account, break-glass
  # IAM users, or CI/CD roles that need full kubectl access).
  #
  # EKS Access Entries replace the legacy aws-auth ConfigMap and
  # support IAM users, roles, and the account root principal.
  #
  # Note: the root account ARN must be in the format
  #   "arn:aws:iam::<account-id>:root"
  # ----------------------------------------------------------
  access_entries = {
    for idx, arn in var.admin_iam_principals :
    "admin-${idx}" => {
      principal_arn = arn

      policy_associations = {
        admin = {
          policy_arn = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
          access_scope = {
            type = "cluster"
          }
        }
      }
    }
  }

  tags = local.common_tags
}

# ==============================================================
# IAM Role for EBS CSI Driver (IRSA)
# ==============================================================
# Allows the EBS CSI Driver to create/delete EBS volumes on AWS.
# Uses IRSA instead of attaching the policy to the node IAM role
# (more secure — least-privilege per workload).
# ==============================================================

module "ebs_csi_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name             = "${local.cluster_name}-ebs-csi-driver"
  attach_ebs_csi_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:ebs-csi-controller-sa"]
    }
  }

  tags = local.common_tags
}

# ==============================================================
# IAM Role for AWS Load Balancer Controller (IRSA)
# ==============================================================
# The AWS Load Balancer Controller creates an ALB when you deploy
# an Ingress resource with ingressClassName: alb. This IAM role
# allows the controller to create and manage ALBs on AWS.
# ==============================================================

module "aws_load_balancer_controller_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name                              = "${local.cluster_name}-aws-load-balancer-controller"
  attach_load_balancer_controller_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }

  tags = local.common_tags
}

# ==============================================================
# AWS Load Balancer Controller — installed via Helm
# ==============================================================
# Reads Kubernetes Ingress resources (ingressClassName: alb) and
# provisions an ALB on AWS. Uses IRSA (the role above) to call AWS
# APIs without node-level credentials.
# ==============================================================

resource "helm_release" "aws_load_balancer_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "~> 1.8"

  # Required: tell the controller which cluster to manage
  set {
    name  = "clusterName"
    value = module.eks.cluster_name
  }

  # Create the ServiceAccount and annotate it with the IRSA role
  set {
    name  = "serviceAccount.create"
    value = "true"
  }
  set {
    name  = "serviceAccount.name"
    value = "aws-load-balancer-controller"
  }
  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.aws_load_balancer_controller_irsa.iam_role_arn
  }

  # Scope the controller to the correct region and VPC
  set {
    name  = "region"
    value = var.aws_region
  }
  set {
    name  = "vpcId"
    value = module.vpc.vpc_id
  }

  depends_on = [module.eks]
}
