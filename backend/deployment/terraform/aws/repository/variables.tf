# ==============================================================
# Variables
# ==============================================================

variable "aws_region" {
  description = "AWS region where the ECR repositories will be created"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name — used as the ECR repository namespace prefix (e.g. 'atlas')"
  type        = string
  default     = "atlas"
}

variable "services" {
  description = "List of application service names that will each get a private ECR repository"
  type        = list(string)
  default = [
    "api-gateway",
    "authorization-server",
    "user-service",
    "catalog-service",
    "inventory-service",
    "order-service",
    "payment-service",
  ]
}

variable "image_tag_mutability" {
  description = "Tag mutability setting for all repositories: MUTABLE or IMMUTABLE"
  type        = string
  default     = "MUTABLE"

  validation {
    condition     = contains(["MUTABLE", "IMMUTABLE"], var.image_tag_mutability)
    error_message = "Allowed values: MUTABLE, IMMUTABLE."
  }
}

variable "scan_on_push" {
  description = "Whether ECR should automatically scan images for vulnerabilities on push"
  type        = bool
  default     = true
}

variable "untagged_image_expiry_days" {
  description = "Number of days after which untagged images are automatically deleted (0 = disabled)"
  type        = number
  default     = 14

  validation {
    condition     = var.untagged_image_expiry_days >= 0
    error_message = "Must be 0 (disabled) or a positive number of days."
  }
}

variable "keep_last_n_tagged_images" {
  description = "Number of most-recent tagged images to keep per repository (0 = keep all)"
  type        = number
  default     = 10

  validation {
    condition     = var.keep_last_n_tagged_images >= 0
    error_message = "Must be 0 (keep all) or a positive number."
  }
}
