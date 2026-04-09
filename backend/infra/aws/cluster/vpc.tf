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
