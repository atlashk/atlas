terraform {
  required_version = ">= 1.9"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
  }

  # -------------------------------------------------------
  # Remote State — values are injected by install.eks.sh
  # via -backend-config flags at `terraform init` time.
  # -------------------------------------------------------
  backend "s3" {}
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
# Provider: Helm
# Used to install add-ons via Helm charts (e.g. AWS Load Balancer Controller)
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
