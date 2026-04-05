# ==============================================================
# EKS Cluster information
# ==============================================================

output "cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "API server endpoint of the EKS cluster"
  value       = module.eks.cluster_endpoint
}

output "cluster_version" {
  description = "Kubernetes version running on the cluster"
  value       = module.eks.cluster_version
}

output "cluster_oidc_issuer_url" {
  description = "OIDC issuer URL of the cluster (used for IRSA)"
  value       = module.eks.cluster_oidc_issuer_url
}

# ==============================================================
# Utility command — copy and run directly
# ==============================================================

output "configure_kubectl" {
  description = "Run this command to configure kubectl to connect to the cluster"
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}"
}

# ==============================================================
# VPC information
# ==============================================================

output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "IDs of the private subnets (where EKS nodes run)"
  value       = module.vpc.private_subnets
}

output "public_subnet_ids" {
  description = "IDs of the public subnets (where Load Balancers are created)"
  value       = module.vpc.public_subnets
}

# ==============================================================
# IAM Role ARNs — used when installing Helm charts
# ==============================================================

output "ebs_csi_driver_role_arn" {
  description = "IAM Role ARN for the EBS CSI Driver"
  value       = module.ebs_csi_irsa.iam_role_arn
}

output "aws_load_balancer_controller_role_arn" {
  description = "IAM Role ARN for the AWS Load Balancer Controller (used when installing the Helm chart)"
  value       = module.aws_load_balancer_controller_irsa.iam_role_arn
}

# ==============================================================
# Summary — quick reference after apply
# ==============================================================

output "summary" {
  description = "Cluster summary"
  value = {
    cluster_name = module.eks.cluster_name
    region       = var.aws_region
    environment  = var.environment
    k8s_version  = module.eks.cluster_version
    vpc_id       = module.vpc.vpc_id
  }
}
