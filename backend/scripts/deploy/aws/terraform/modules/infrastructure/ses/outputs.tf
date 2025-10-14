output "domain_identity_arn" {
  description = "ARN of the SES domain identity"
  value       = aws_ses_domain_identity.main.arn
}

output "domain_identity_verification_token" {
  description = "Verification token for the SES domain identity"
  value       = aws_ses_domain_identity.main.verification_token
}

output "dkim_tokens" {
  description = "DKIM tokens for the domain"
  value       = aws_ses_domain_dkim.main.dkim_tokens
}

output "mail_from_domain" {
  description = "Mail from domain"
  value       = aws_ses_domain_mail_from.main.mail_from_domain
}

output "configuration_set_name" {
  description = "Name of the SES configuration set"
  value       = aws_ses_configuration_set.main.name
}

output "configuration_set_arn" {
  description = "ARN of the SES configuration set"
  value       = aws_ses_configuration_set.main.arn
}

output "sending_role_arn" {
  description = "ARN of the IAM role for SES sending"
  value       = aws_iam_role.ses_sending_role.arn
}

output "sending_role_name" {
  description = "Name of the IAM role for SES sending"
  value       = aws_iam_role.ses_sending_role.name
}

output "sending_policy_arn" {
  description = "ARN of the IAM policy for SES sending"
  value       = aws_iam_policy.ses_sending_policy.arn
}

output "domain_name" {
  description = "Domain name configured for SES"
  value       = var.domain_name
}

output "smtp_endpoint" {
  description = "SMTP endpoint for the current region"
  value       = "email-smtp.${data.aws_region.current.name}.amazonaws.com"
}

output "smtp_port" {
  description = "SMTP port for TLS connection"
  value       = 587
}