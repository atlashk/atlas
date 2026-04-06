# ==============================================================
# Terraform Backend Bootstrap
#
# Purpose: Creates the S3 bucket and DynamoDB table that will
#          store the Terraform state for all other modules.
#
# Run ONCE before any other Terraform module:
#   cd init/
#   terraform init
#   terraform apply
#
# After apply, uncomment the backend "s3" block in
# ../cluster/versions.tf and run `terraform init -migrate-state`
# ==============================================================

# --------------------------------------------------------------
# S3 Bucket — stores the remote state files
# --------------------------------------------------------------
resource "aws_s3_bucket" "terraform_state" {
  bucket = local.state_bucket_name

  # Prevent accidental deletion when the bucket contains state files
  lifecycle {
    prevent_destroy = true
  }

  tags = {
    Name        = local.state_bucket_name
    Description = "Stores Terraform remote state for ${var.project_name}"
  }
}

resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    id     = "expire-old-noncurrent-versions"
    status = "Enabled"

    filter {} # required by AWS provider v5 — matches all objects

    noncurrent_version_expiration {
      # Keep the last 10 non-current versions, delete anything older than 90 days
      newer_noncurrent_versions = 10
      noncurrent_days           = 90
    }
  }
}

# --------------------------------------------------------------
# DynamoDB Table — provides state locking & consistency checks
# --------------------------------------------------------------
resource "aws_dynamodb_table" "terraform_state_lock" {
  name         = local.lock_table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  # Enable point-in-time recovery for safety
  point_in_time_recovery {
    enabled = true
  }

  lifecycle {
    prevent_destroy = true
  }

  tags = {
    Name        = local.lock_table_name
    Description = "Terraform state lock table for ${var.project_name}"
  }
}
