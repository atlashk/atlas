# Staging Backend Configuration
bucket         = "atlas-terraform-state-staging"
key            = "staging/terraform.tfstate"
region         = "us-west-2"
dynamodb_table = "atlas-terraform-locks-staging"
encrypt        = true