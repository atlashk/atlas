variable "name_prefix" {
  description = "Prefix for naming resources"
  type        = string
}

variable "service_name" {
  description = "Name of the service (e.g., order, payment, product, user)"
  type        = string
}

variable "msk_cluster_arn" {
  description = "ARN of the MSK cluster"
  type        = string
  default     = ""
}

variable "s3_bucket_arn" {
  description = "ARN of the S3 bucket for file storage"
  type        = string
  default     = ""
}

variable "enable_msk_access" {
  description = "Enable MSK (Kafka) access for this service"
  type        = bool
  default     = true
}

variable "enable_s3_access" {
  description = "Enable S3 access for this service"
  type        = bool
  default     = false
}

variable "enable_ses_access" {
  description = "Enable SES (email) access for this service"
  type        = bool
  default     = false
}

variable "enable_xray_tracing" {
  description = "Enable X-Ray tracing for this service"
  type        = bool
  default     = true
}

variable "enable_secrets_access" {
  description = "Enable AWS Secrets Manager access for this service"
  type        = bool
  default     = false
}

variable "secrets_arns" {
  description = "List of secret ARNs that this service needs access to"
  type        = list(string)
  default     = []
}

variable "secrets_kms_key_ids" {
  description = "List of KMS key IDs used to encrypt the secrets"
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}