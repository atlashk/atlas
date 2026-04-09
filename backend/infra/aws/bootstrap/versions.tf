terraform {
  required_version = ">= 1.9"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # The backend bootstrap module itself uses LOCAL state.
  # The resulting tfstate file is small (only 2 resources) and
  # should be committed to git so the team can manage the bucket/table.
  # Alternatively, store it manually in a secure location.
}
