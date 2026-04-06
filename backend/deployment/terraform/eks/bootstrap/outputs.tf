# ==============================================================
# Outputs — values needed to configure the S3 backend in other
#           Terraform modules (e.g. eks/cluster)
# ==============================================================

output "state_bucket_name" {
  description = "Name of the S3 bucket that stores Terraform state files"
  value       = aws_s3_bucket.terraform_state.id
}

output "state_bucket_arn" {
  description = "ARN of the S3 state bucket"
  value       = aws_s3_bucket.terraform_state.arn
}

output "state_bucket_region" {
  description = "AWS region of the S3 state bucket"
  value       = aws_s3_bucket.terraform_state.region
}

output "lock_table_name" {
  description = "Name of the DynamoDB table used for state locking"
  value       = aws_dynamodb_table.terraform_state_lock.name
}

output "lock_table_arn" {
  description = "ARN of the DynamoDB state-lock table"
  value       = aws_dynamodb_table.terraform_state_lock.arn
}

output "backend_config_snippet" {
  description = "Ready-to-paste backend block for other Terraform modules"
  value       = <<-EOT
    # Paste this into your module's versions.tf → terraform { ... }

    backend "s3" {
      bucket         = "${aws_s3_bucket.terraform_state.id}"
      key            = "<module>/terraform.tfstate"   # e.g. "atlas/eks/terraform.tfstate"
      region         = "${aws_s3_bucket.terraform_state.region}"
      dynamodb_table = "${aws_dynamodb_table.terraform_state_lock.name}"
      encrypt        = true
    }
  EOT
}
