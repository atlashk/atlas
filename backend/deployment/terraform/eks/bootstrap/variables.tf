# ==============================================================
# Variables
# ==============================================================

variable "aws_region" {
  description = "AWS region where the S3 bucket and DynamoDB table will be created"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name — used as a prefix for all resource names"
  type        = string
  default     = "atlas"
}

variable "state_bucket_suffix" {
  description = <<-EOT
    Optional suffix appended to the S3 bucket name to make it globally unique.
    S3 bucket names must be unique across all AWS accounts.
    Example: "abc123" → bucket name becomes "<project>-terraform-state-abc123"
    Leave empty to use the default name without a suffix.
  EOT
  type        = string
  default     = ""
}
