# SES (Simple Email Service) Terraform Module

This module creates and configures AWS Simple Email Service (SES) for sending emails from your application.

## Features

- SES domain identity verification
- DKIM configuration for email authentication
- Mail-from domain configuration
- Route53 DNS records for domain verification (optional)
- SES configuration set with CloudWatch monitoring
- IAM roles and policies for secure email sending
- Support for bounce and complaint notifications

## Usage

```hcl
module "ses" {
  source = "./modules/infrastructure/ses"
  
  name_prefix    = "myapp-prod"
  domain_name    = "example.com"
  route53_zone_id = "Z1234567890ABC"  # Optional
  
  from_addresses = [
    "noreply@example.com",
    "support@example.com"
  ]
  
  allowed_senders = [
    "arn:aws:iam::123456789012:root"
  ]
  
  tags = {
    Environment = "production"
    Project     = "myapp"
  }
}
```

## Requirements

- AWS Provider >= 5.0
- Terraform >= 1.0

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| name_prefix | Name prefix for all resources | `string` | n/a | yes |
| domain_name | Domain name for SES identity | `string` | n/a | yes |
| route53_zone_id | Route53 hosted zone ID for DNS records | `string` | `null` | no |
| from_addresses | List of allowed from email addresses | `list(string)` | `[]` | no |
| allowed_senders | List of AWS account IDs or ARNs allowed to send emails | `list(string)` | `[]` | no |
| trusted_services | List of AWS services that can assume the SES sending role | `list(string)` | `["ec2.amazonaws.com", "ecs-tasks.amazonaws.com", "lambda.amazonaws.com"]` | no |
| tags | Tags to apply to all resources | `map(string)` | `{}` | no |

## Outputs

| Name | Description |
|------|-------------|
| domain_identity_arn | ARN of the SES domain identity |
| domain_identity_verification_token | Verification token for the SES domain identity |
| dkim_tokens | DKIM tokens for the domain |
| mail_from_domain | Mail from domain |
| configuration_set_name | Name of the SES configuration set |
| configuration_set_arn | ARN of the SES configuration set |
| sending_role_arn | ARN of the IAM role for SES sending |
| sending_role_name | Name of the IAM role for SES sending |
| sending_policy_arn | ARN of the IAM policy for SES sending |
| smtp_endpoint | SMTP endpoint for the current region |
| smtp_port | SMTP port for TLS connection |

## DNS Configuration

If you provide a `route53_zone_id`, the module will automatically create the necessary DNS records for:

1. Domain verification (TXT record)
2. DKIM authentication (CNAME records)
3. Mail-from domain (MX and TXT records)

If you don't use Route53, you'll need to manually create these DNS records using the output values.

## Email Sending

After the domain is verified, you can send emails using:

1. **AWS SDK**: Use the SES API with the IAM role created by this module
2. **SMTP**: Use the SMTP endpoint and credentials (create SMTP credentials in AWS Console)

## Monitoring

The module creates a CloudWatch event destination that tracks:
- Email sends
- Bounces
- Complaints
- Deliveries
- Rejects

## Security

- IAM policies follow the principle of least privilege
- Only specified from addresses are allowed
- Only trusted AWS services can assume the sending role
- TLS is required for all email delivery