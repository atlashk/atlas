# Stripe Secrets Module
# This module creates AWS Secrets Manager secrets for Stripe API keys and webhook secrets

# KMS Key for encrypting Stripe secrets
resource "aws_kms_key" "stripe_secrets" {
  description             = "KMS key for encrypting Stripe secrets"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-stripe-secrets-kms-key"
  })
}

resource "aws_kms_alias" "stripe_secrets" {
  name          = "alias/${var.name_prefix}-stripe-secrets"
  target_key_id = aws_kms_key.stripe_secrets.key_id
}

# Stripe Secret Key
resource "aws_secretsmanager_secret" "stripe_secret_key" {
  name                    = "${var.name_prefix}-stripe-secret-key"
  description             = "Stripe secret key for payment processing"
  kms_key_id              = aws_kms_key.stripe_secrets.arn
  recovery_window_in_days = 7

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-stripe-secret-key"
  })
}

resource "aws_secretsmanager_secret_version" "stripe_secret_key" {
  count         = var.stripe_secret_key != "" ? 1 : 0
  secret_id     = aws_secretsmanager_secret.stripe_secret_key.id
  secret_string = var.stripe_secret_key
}

# Stripe Publishable Key
resource "aws_secretsmanager_secret" "stripe_publishable_key" {
  name                    = "${var.name_prefix}-stripe-publishable-key"
  description             = "Stripe publishable key for client-side payment processing"
  kms_key_id              = aws_kms_key.stripe_secrets.arn
  recovery_window_in_days = 7

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-stripe-publishable-key"
  })
}

resource "aws_secretsmanager_secret_version" "stripe_publishable_key" {
  count         = var.stripe_publishable_key != "" ? 1 : 0
  secret_id     = aws_secretsmanager_secret.stripe_publishable_key.id
  secret_string = var.stripe_publishable_key
}

# Stripe Webhook Endpoint Secret
resource "aws_secretsmanager_secret" "stripe_webhook_endpoint_secret" {
  name                    = "${var.name_prefix}-stripe-webhook-endpoint-secret"
  description             = "Stripe webhook endpoint secret for signature verification"
  kms_key_id              = aws_kms_key.stripe_secrets.arn
  recovery_window_in_days = 7

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-stripe-webhook-endpoint-secret"
  })
}

resource "aws_secretsmanager_secret_version" "stripe_webhook_endpoint_secret" {
  count         = var.stripe_webhook_endpoint_secret != "" ? 1 : 0
  secret_id     = aws_secretsmanager_secret.stripe_webhook_endpoint_secret.id
  secret_string = var.stripe_webhook_endpoint_secret
}

# IAM Policy for accessing Stripe secrets
resource "aws_iam_policy" "stripe_secrets_access" {
  name        = "${var.name_prefix}-stripe-secrets-access"
  description = "IAM policy for accessing Stripe secrets"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = [
          aws_secretsmanager_secret.stripe_secret_key.arn,
          aws_secretsmanager_secret.stripe_publishable_key.arn,
          aws_secretsmanager_secret.stripe_webhook_endpoint_secret.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:DescribeKey"
        ]
        Resource = [
          aws_kms_key.stripe_secrets.arn
        ]
      }
    ]
  })

  tags = var.tags
}