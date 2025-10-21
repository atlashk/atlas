# Bootstrap Module Outputs

output "terraform_state_bucket" {
  description = "Name of the S3 bucket for Terraform state"
  value       = aws_s3_bucket.terraform_state.bucket
}

output "terraform_state_bucket_arn" {
  description = "ARN of the S3 bucket for Terraform state"
  value       = aws_s3_bucket.terraform_state.arn
}

output "kms_key_id" {
  description = "ID of the KMS key for state encryption"
  value       = aws_kms_key.terraform_state.key_id
}

output "kms_key_arn" {
  description = "ARN of the KMS key for state encryption"
  value       = aws_kms_key.terraform_state.arn
}

# Backend configuration template (Terraform 1.11+ with S3 native locking)
output "backend_config" {
  description = "Backend configuration to add to your main Terraform configuration"
  value = <<-EOT
    terraform {
      backend "s3" {
        bucket     = "${aws_s3_bucket.terraform_state.bucket}"
        key        = "infrastructure/terraform.tfstate"
        region     = "${var.aws_region}"
        encrypt    = true
        kms_key_id = "${aws_kms_key.terraform_state.arn}"
      }
    }
  EOT
}
