terraform {
  required_version = ">= 1.9"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.17"
    }
  }

  # -------------------------------------------------------
  # Remote State — values are injected by install.eks.sh
  # via -backend-config flags at `terraform init` time.
  # -------------------------------------------------------
  backend "s3" {}
}
