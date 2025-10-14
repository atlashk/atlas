variable "name_prefix" {
  description = "Name prefix for all resources"
  type        = string
}

variable "domain_name" {
  description = "Domain name for SES identity"
  type        = string
}

variable "route53_zone_id" {
  description = "Route53 hosted zone ID for DNS records (optional)"
  type        = string
  default     = null
}

variable "from_addresses" {
  description = "List of allowed from email addresses"
  type        = list(string)
  default     = []
}

variable "allowed_senders" {
  description = "List of AWS account IDs or ARNs allowed to send emails"
  type        = list(string)
  default     = []
}

variable "trusted_services" {
  description = "List of AWS services that can assume the SES sending role"
  type        = list(string)
  default     = [
    "ec2.amazonaws.com",
    "ecs-tasks.amazonaws.com",
    "lambda.amazonaws.com"
  ]
}



variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default     = {}
}