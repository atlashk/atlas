# Production Backend Configuration
bucket         = "atlas-terraform-state-prod"
key            = "prod/terraform.tfstate"
region         = "us-west-2"
dynamodb_table = "atlas-terraform-locks-prod"
encrypt        = true