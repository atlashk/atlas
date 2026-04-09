terraform {
  required_version = ">= 1.9"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # -------------------------------------------------------
  # Remote State — values are injected by the deploy script
  # via -backend-config flags at `terraform init` time.
  # -------------------------------------------------------
  backend "s3" {}
}
