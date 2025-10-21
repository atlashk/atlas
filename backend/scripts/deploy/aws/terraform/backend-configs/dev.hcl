# Development Backend Configuration (Terraform 1.11+ with S3 native locking)
bucket  = "atlas-terraform-state-dev"
key     = "dev/terraform.tfstate"
region  = "us-east-1"
encrypt = true
