# ==============================================================
# General
# ==============================================================

variable "aws_region" {
  description = "AWS region to deploy the EKS cluster (e.g. ap-southeast-1 = Singapore)"
  type        = string
  default     = "ap-southeast-1"
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
  default     = "1.31"
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
# Node Group: System
# Purpose: runs Kubernetes add-ons (CoreDNS, metrics-server...)
# Small instances — low load, not intended for business logic
# ==============================================================

variable "system_node_group" {
  description = "Configuration for the Kubernetes system components node group"
  type = object({
    instance_types = list(string)
    min_size       = number
    max_size       = number
    desired_size   = number
    disk_size_gb   = number
  })
  default = {
    instance_types = ["t3.medium"]
    min_size       = 1
    max_size       = 2
    desired_size   = 1
    disk_size_gb   = 20
  }
}

# ==============================================================
# Node Group: app
# Purpose: runs the Atlas microservices
#   api-gateway, catalog-service, inventory-service,
#   order-service, payment-service, user-service,
#   authorization-server, config-server, discovery-server
# ==============================================================

variable "app_node_group" {
  description = "Configuration for the microservices node group"
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
    max_size       = 2
    desired_size   = 1
    disk_size_gb   = 30
  }
}

# ==============================================================
# Node Group: infra
# Purpose: runs stateful services
#   MySQL, Redis, Kafka, Elasticsearch, MinIO,
#   Loki, Prometheus, Tempo
# Larger instances required due to higher RAM/disk demands
# ==============================================================

variable "infra_node_group" {
  description = "Configuration for the stateful infrastructure services node group"
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
    max_size       = 2
    desired_size   = 1
    disk_size_gb   = 30
  }
}
