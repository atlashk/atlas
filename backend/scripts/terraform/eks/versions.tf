terraform {
  required_version = ">= 1.9"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
  }

  # -------------------------------------------------------
  # Remote State (uncomment once the S3 bucket is created)
  # Strongly recommended for team collaboration & state safety
  # -------------------------------------------------------
  # backend "s3" {
  #   bucket         = "atlas-terraform-state"   # <-- replace with your bucket name
  #   key            = "atlas/eks/terraform.tfstate"
  #   region         = "ap-southeast-1"
  #   dynamodb_table = "atlas-terraform-state-lock"
  #   encrypt        = true
  # }
}

# -------------------------------------------------------
# Provider: AWS
# -------------------------------------------------------
provider "aws" {
  region = var.aws_region

  # Default tags automatically applied to all AWS resources
  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# -------------------------------------------------------
# Provider: Kubernetes
# Connects to the EKS cluster once it has been provisioned
# -------------------------------------------------------
provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name, "--region", var.aws_region]
  }
}

# -------------------------------------------------------
# Provider: Helm
# Used to install add-ons via Helm charts (e.g. AWS LB Controller)
# -------------------------------------------------------
provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name, "--region", var.aws_region]
    }
  }
}
