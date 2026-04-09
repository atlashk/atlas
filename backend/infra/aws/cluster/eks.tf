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
  # Managed Node Group: default
  # Single node group that runs all workloads:
  #   Kubernetes add-ons, microservices, and stateful services
  # ----------------------------------------------------------
  eks_managed_node_groups = {
    default = {
      name           = "${local.cluster_name}-default"
      instance_types = var.node_group.instance_types
      min_size       = var.node_group.min_size
      max_size       = var.node_group.max_size
      desired_size   = var.node_group.desired_size
      disk_size      = var.node_group.disk_size_gb
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
