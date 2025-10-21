# Stripe Secrets Module Outputs

output "stripe_secret_key_arn" {
  description = "ARN of the Stripe secret key secret"
  value       = aws_secretsmanager_secret.stripe_secret_key.arn
  sensitive   = true
}

output "stripe_publishable_key_arn" {
  description = "ARN of the Stripe publishable key secret"
  value       = aws_secretsmanager_secret.stripe_publishable_key.arn
  sensitive   = true
}

output "stripe_webhook_endpoint_secret_arn" {
  description = "ARN of the Stripe webhook endpoint secret"
  value       = aws_secretsmanager_secret.stripe_webhook_endpoint_secret.arn
  sensitive   = true
}

output "stripe_secrets_kms_key_id" {
  description = "KMS key ID used to encrypt Stripe secrets"
  value       = aws_kms_key.stripe_secrets.key_id
}

output "stripe_secrets_kms_key_arn" {
  description = "KMS key ARN used to encrypt Stripe secrets"
  value       = aws_kms_key.stripe_secrets.arn
}

output "stripe_secrets_access_policy_arn" {
  description = "IAM policy ARN for accessing Stripe secrets"
  value       = aws_iam_policy.stripe_secrets_access.arn
}