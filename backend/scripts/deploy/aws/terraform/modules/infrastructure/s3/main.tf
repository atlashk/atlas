# Random suffix for bucket name
resource "random_string" "bucket_suffix" {
  length  = 8
  special = false
  upper   = false
}

# Product Image S3 Bucket
resource "aws_s3_bucket" "product_image" {
  bucket = "${var.name_prefix}-product-image-${random_string.bucket_suffix.result}"

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-product-image"
  })
}

# Product Image S3 Bucket Versioning
resource "aws_s3_bucket_versioning" "product_image" {
  bucket = aws_s3_bucket.product_image.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Product Image S3 Bucket Server Side Encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "product_image" {
  bucket = aws_s3_bucket.product_image.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

# Product Image S3 Bucket Public Access Block
resource "aws_s3_bucket_public_access_block" "product_image" {
  bucket = aws_s3_bucket.product_image.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Product Image S3 Bucket Lifecycle Configuration
resource "aws_s3_bucket_lifecycle_configuration" "product_image" {
  bucket = aws_s3_bucket.product_image.id

  rule {
    id     = "delete_incomplete_multipart_uploads"
    status = "Enabled"

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }

  rule {
    id     = "transition_to_ia"
    status = "Enabled"

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }

    transition {
      days          = 90
      storage_class = "GLACIER"
    }

    expiration {
      days = 365
    }

    noncurrent_version_expiration {
      noncurrent_days = 30
    }
  }
}

# IAM Policy for Product Image S3 Access
resource "aws_iam_policy" "product_image_s3_access" {
  name        = "${var.name_prefix}-product-image-s3-access"
  description = "Policy for accessing Product Image S3 bucket"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.product_image.arn,
          "${aws_s3_bucket.product_image.arn}/*"
        ]
      }
    ]
  })

  tags = var.tags
}