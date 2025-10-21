# Stripe Secrets Module Variables

variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "stripe_secret_key" {
  description = "Stripe secret key for payment processing"
  type        = string
  default     = ""
  sensitive   = true
}

variable "stripe_publishable_key" {
  description = "Stripe publishable key for client-side payment processing"
  type        = string
  default     = ""
  sensitive   = true
}

variable "stripe_webhook_endpoint_secret" {
  description = "Stripe webhook endpoint secret for signature verification"
  type        = string
  default     = ""
  sensitive   = true
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}