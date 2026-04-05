# Fetch available AZs in the selected region
data "aws_availability_zones" "available" {
  filter {
    name   = "opt-in-status"
    values = ["opt-in-not-required"]
  }
}

# Fetch current AWS account information (account ID, ARN, user ID)
data "aws_caller_identity" "current" {}

locals {
  # Cluster name convention: {project}-{env}
  # Example: atlas-dev, atlas-prod
  cluster_name = "${var.project_name}-${var.environment}"

  # Slice AZ list to the requested count
  azs = slice(data.aws_availability_zones.available.names, 0, var.availability_zones_count)

  # Common tags applied to all resources
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ClusterName = local.cluster_name
  }
}
