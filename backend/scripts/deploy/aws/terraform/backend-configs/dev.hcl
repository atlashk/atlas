# Development Backend Configuration
bucket         = "atlas-terraform-state-dev"
key            = "dev/terraform.tfstate"
region         = "us-west-2"
dynamodb_table = "atlas-terraform-locks-dev"
encrypt        = true