# ==============================================================
# General
# ==============================================================

variable "aws_region" {
  description = "AWS region to deploy the EKS cluster (e.g. us-east-1 = Singapore)"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name — used as a prefix for all AWS resource names"
  type        = string
  default     = "atlas"
}

variable "environment" {
  description = "Deployment environment: dev | staging | production"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "staging", "production"], var.environment)
    error_message = "Allowed values: dev, staging, production."
  }
}

# ==============================================================
# Kubernetes
# ==============================================================

variable "kubernetes_version" {
  description = "Kubernetes version for EKS (see: https://docs.aws.amazon.com/eks/latest/userguide/kubernetes-versions.html)"
  type        = string
  default     = "1.35"
}

# ==============================================================
# Networking (VPC)
# ==============================================================

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones_count" {
  description = "Number of Availability Zones to use (minimum 2 for high availability)"
  type        = number
  default     = 2

  validation {
    condition     = var.availability_zones_count >= 2 && var.availability_zones_count <= 3
    error_message = "Must be between 2 and 3 AZs."
  }
}

# ==============================================================
# Access Control
# ==============================================================

variable "admin_iam_principals" {
  description = <<-EOT
    List of IAM principal ARNs (IAM users, roles, or the root account) that will
    receive cluster-admin (AmazonEKSClusterAdminPolicy) access via EKS Access Entries.

    Examples:
      - Root account : "arn:aws:iam::123456789012:root"
      - IAM user     : "arn:aws:iam::123456789012:user/alice"
      - IAM role     : "arn:aws:iam::123456789012:role/my-admin-role"
  EOT
  type        = list(string)
  default     = []
}

# ==============================================================
# Node Group: default
# Purpose: runs all workloads — Kubernetes add-ons, microservices,
#   and stateful services (MySQL, Redis, Kafka, ES, MinIO...)
# ==============================================================

variable "node_group" {
  description = "Configuration for the single default EKS managed node group"
  type = object({
    instance_types = list(string)
    min_size       = number
    max_size       = number
    desired_size   = number
    disk_size_gb   = number
  })
  default = {
    instance_types = ["t3.large"]
    min_size       = 1
    max_size       = 3
    desired_size   = 1
    disk_size_gb   = 30
  }
}
