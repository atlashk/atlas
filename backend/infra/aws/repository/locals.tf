# ==============================================================
# Locals
# ==============================================================

locals {
  # Full repository names follow the convention: <project>/<service>
  # e.g. atlas/api-gateway, atlas/authorization-server, …
  repository_names = {
    for svc in var.services : svc => "${var.project_name}/${svc}"
  }
}
