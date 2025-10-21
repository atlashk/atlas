output "db_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.main.endpoint
}

output "db_port" {
  description = "RDS instance port"
  value       = aws_db_instance.main.port
}

# Output secret ARN instead of plain text password
output "db_secret_arn" {
  description = "ARN of the secret containing database password"
  value       = aws_db_instance.main.master_user_secret[0].secret_arn
  sensitive   = true
}

# IAM policy ARN for ECS tasks to access secrets
output "rds_secrets_policy_arn" {
  description = "IAM policy ARN for accessing RDS secrets"
  value       = aws_iam_policy.rds_secrets_access.arn
}
