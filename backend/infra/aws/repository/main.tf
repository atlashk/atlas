# ==============================================================
# ECR Private Repositories
#
# Creates one private ECR repository per application service.
# Repository names follow the pattern: <project_name>/<service>
#   e.g.  atlas/api-gateway
#         atlas/authorization-server
#         atlas/user-service  …
# ==============================================================

# --------------------------------------------------------------
# Import existing ECR repositories into state (idempotent)
# If a repo already exists in AWS, this prevents a creation error.
# Requires Terraform >= 1.7 (for_each in import blocks).
# --------------------------------------------------------------
import {
  for_each = local.repository_names
  to       = aws_ecr_repository.services[each.key]
  id       = each.value
}

# --------------------------------------------------------------
# Private ECR repositories — one per service
# --------------------------------------------------------------
resource "aws_ecr_repository" "services" {
  for_each = local.repository_names

  name                 = each.value
  image_tag_mutability = var.image_tag_mutability

  image_scanning_configuration {
    scan_on_push = var.scan_on_push
  }

  # Encrypt images at rest using the default AWS-managed key (AES-256)
  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name    = each.value
    Service = each.key
  }
}

# --------------------------------------------------------------
# Lifecycle policies — keep repos from growing unbounded
# --------------------------------------------------------------
resource "aws_ecr_lifecycle_policy" "services" {
  # Only create lifecycle policies when at least one rule is configured.
  # If both vars are 0 the rules list would be empty, which AWS rejects.
  for_each   = (var.untagged_image_expiry_days > 0 || var.keep_last_n_tagged_images > 0) ? aws_ecr_repository.services : {}
  repository = each.value.name

  policy = jsonencode({
    rules = concat(
      # Rule 1: always expire untagged images after N days
      var.untagged_image_expiry_days > 0 ? [
        {
          rulePriority = 1
          description  = "Expire untagged images after ${var.untagged_image_expiry_days} days"
          selection = {
            tagStatus   = "untagged"
            countType   = "sinceImagePushed"
            countUnit   = "days"
            countNumber = var.untagged_image_expiry_days
          }
          action = { type = "expire" }
        }
      ] : [],

      # Rule 2: keep only the last N tagged images per repo
      var.keep_last_n_tagged_images > 0 ? [
        {
          rulePriority = 2
          description  = "Keep only the last ${var.keep_last_n_tagged_images} tagged images"
          selection = {
            tagStatus       = "tagged"
            tagPatternList  = ["*"]
            countType       = "imageCountMoreThan"
            countNumber     = var.keep_last_n_tagged_images
          }
          action = { type = "expire" }
        }
      ] : []
    )
  })
}
