# Staging Backend Configuration (Terraform 1.11+ with S3 native locking)
bucket  = "atlas-terraform-state-staging"
key     = "staging/terraform.tfstate"
region  = "us-east-1"
encrypt = true
