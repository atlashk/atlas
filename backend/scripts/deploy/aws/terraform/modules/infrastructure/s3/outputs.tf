# Product Image Bucket Outputs
output "product_image_bucket_name" {
  description = "Name of the Product Image S3 bucket"
  value       = aws_s3_bucket.product_image.bucket
}

output "product_image_bucket_arn" {
  description = "ARN of the Product Image S3 bucket"
  value       = aws_s3_bucket.product_image.arn
}

output "product_image_bucket_domain_name" {
  description = "Domain name of the Product Image S3 bucket"
  value       = aws_s3_bucket.product_image.bucket_domain_name
}

output "product_image_s3_access_policy_arn" {
  description = "ARN of the Product Image S3 access policy"
  value       = aws_iam_policy.product_image_s3_access.arn
}

# Generic outputs for IAM module compatibility
output "bucket_arn" {
  description = "ARN of the S3 bucket (alias for product_image_bucket_arn)"
  value       = aws_s3_bucket.product_image.arn
}

output "bucket_name" {
  description = "Name of the S3 bucket (alias for product_image_bucket_name)"
  value       = aws_s3_bucket.product_image.bucket
}

output "policy_arn" {
  description = "ARN of the S3 access policy (alias for product_image_s3_access_policy_arn)"
  value       = aws_iam_policy.product_image_s3_access.arn
}