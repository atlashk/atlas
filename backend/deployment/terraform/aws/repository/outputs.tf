# ==============================================================
# Outputs
# ==============================================================

output "repository_urls" {
  description = "Map of service name → ECR repository URL (used in CI/CD push commands)"
  value = {
    for k, repo in aws_ecr_repository.services : k => repo.repository_url
  }
}

output "repository_arns" {
  description = "Map of service name → ECR repository ARN"
  value = {
    for k, repo in aws_ecr_repository.services : k => repo.arn
  }
}

output "registry_id" {
  description = "AWS account ID that owns the ECR registry (same for all repositories)"
  value       = values(aws_ecr_repository.services)[0].registry_id
}
