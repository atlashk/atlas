# ==============================================================
# Locals
# ==============================================================

locals {
  # S3 bucket name — must be globally unique across all AWS accounts.
  # Append an optional suffix when there is a naming conflict.
  state_bucket_name = var.state_bucket_suffix != "" ? (
    "${var.project_name}-terraform-state-${var.state_bucket_suffix}"
  ) : "${var.project_name}-terraform-state"

}
