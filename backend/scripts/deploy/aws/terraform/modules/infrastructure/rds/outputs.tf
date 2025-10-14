output "db_instance_id" {
  description = "ID of the RDS instance"
  value       = aws_db_instance.main.id
}

output "db_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.main.endpoint
}

output "db_port" {
  description = "RDS instance port"
  value       = aws_db_instance.main.port
}

output "db_name" {
  description = "Name of the database"
  value       = aws_db_instance.main.db_name
}

output "db_username" {
  description = "Username for the database"
  value       = aws_db_instance.main.username
  sensitive   = true
}

# Output secret ARN instead of plain text password
output "db_secret_arn" {
  description = "ARN of the secret containing database password"
  value       = aws_db_instance.main.master_user_secret[0].secret_arn
  sensitive   = true
}

output "db_secret_kms_key_id" {
  description = "KMS key ID used to encrypt the database secret"
  value       = aws_kms_key.rds_secrets.key_id
}

# IAM policy ARN for ECS tasks to access secrets
output "rds_secrets_policy_arn" {
  description = "IAM policy ARN for accessing RDS secrets"
  value       = aws_iam_policy.rds_secrets_access.arn
}
