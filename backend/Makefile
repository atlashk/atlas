# ──────────────────────────────────────────────────────────────────────────────
# Shell — use bash for all recipes, and run each target in a single shell
# ──────────────────────	printf '\n===> Step 1/5 — Terraform bootstrap (remote state backend)\n'───────────────────────────────────────────────────────
SHELL       := bash
.SHELLFLAGS := -euo pipefail -c
.ONESHELL:

# ──────────────────────────────────────────────────────────────────────────────
# Paths (resolved relative to this Makefile)
# ──────────────────────────────────────────────────────────────────────────────
MAKEFILE_DIR     := $(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
TF_BOOTSTRAP_DIR := $(MAKEFILE_DIR)/infra/aws/bootstrap
TF_CLUSTER_DIR   := $(MAKEFILE_DIR)/infra/aws/cluster
TF_REPO_DIR      := $(MAKEFILE_DIR)/infra/aws/repository
HELM_CHART_DIR    := $(MAKEFILE_DIR)/charts
HELM_SUBCHART_DIR := $(HELM_CHART_DIR)/charts
STATE_DIR         := $(MAKEFILE_DIR)/.state

# ──────────────────────────────────────────────────────────────────────────────
# Configurable defaults (override on the command line)
# ──────────────────────────────────────────────────────────────────────────────
HELM_RELEASE_NAME     := atlas-dev
HELM_NS_INFRA         := infra
HELM_NS_SECURITY      := security
HELM_NS_OBSERVABILITY := observability
HELM_NS_APP           := app

# ──────────────────────────────────────────────────────────────────────────────
.PHONY: install uninstall help check-prereqs \
        bootstrap cluster repository build push \
        helm helm-common helm-infra helm-security helm-observability helm-app \
        destroy-helm destroy-repository destroy-cluster destroy-bootstrap

.DEFAULT_GOAL := help

# ──────────────────────────────────────────────────────────────────────────────
# Shell helper functions — sourced automatically in every recipe via
# the SHELL_HELPERS variable (exported to the environment).
# ──────────────────────────────────────────────────────────────────────────────
define SHELL_HELPERS
log_info()  { printf '[INFO]  %s\n' "$$1"; }
log_ok()    { printf '[OK]    %s\n' "$$1"; }
log_warn()  { printf '[WARN]  %s\n' "$$1" >&2; }
log_error() { printf '[ERROR] %s\n' "$$1" >&2; }

require_tfvars() {
  if [[ ! -f "$$1/terraform.tfvars" ]]; then
    log_error "terraform.tfvars not found in $$1"
    log_error "cp $$1/terraform.tfvars.example $$1/terraform.tfvars"
    exit 1
  fi
}

load_bootstrap_state() {
  if [[ ! -f "$(STATE_DIR)/bootstrap_bucket" ]]; then
    log_error "Bootstrap state not found. Run 'make bootstrap' first."
    exit 1
  fi
  TF_BOOTSTRAP_BUCKET=$$(cat "$(STATE_DIR)/bootstrap_bucket")
  TF_BOOTSTRAP_REGION=$$(cat "$(STATE_DIR)/bootstrap_region")
}

load_build_state() {
  if [[ ! -f "$(STATE_DIR)/image_tag" ]]; then
    log_error "Build state not found. Run 'make build' first."
    exit 1
  fi
  IMAGE_TAG=$$(cat "$(STATE_DIR)/image_tag")
}

load_kubectl_state() {
  if [[ ! -f "$(STATE_DIR)/configure_kubectl" ]]; then
    log_error "Cluster state not found. Run 'make cluster' first."
    exit 1
  fi
  TF_CONFIGURE_KUBECTL=$$(cat "$(STATE_DIR)/configure_kubectl")
}

load_push_state() {
  if [[ ! -f "$(STATE_DIR)/ecr_registry" ]]; then
    log_error "Push state not found. Run 'make push' first."
    exit 1
  fi
  ECR_REGISTRY=$$(cat "$(STATE_DIR)/ecr_registry")
  IMAGE_TAG=$$(cat "$(STATE_DIR)/image_tag")
}

configure_kubectl() {
  log_info "Configuring kubectl context"
  eval "$$TF_CONFIGURE_KUBECTL"
  log_ok "kubectl context updated"
}

# tf_init <dir> <state-key>   (uses TF_BOOTSTRAP_BUCKET/REGION)
tf_init() {
  local dir="$$1" key="$$2"
  log_info "terraform init ($$dir)"
  terraform -chdir="$$dir" init -input=false -migrate-state -force-copy \
    -backend-config="bucket=$$TF_BOOTSTRAP_BUCKET" \
    -backend-config="key=$(HELM_RELEASE_NAME)/$$key/terraform.tfstate" \
    -backend-config="region=$$TF_BOOTSTRAP_REGION" \
    -backend-config="use_lockfile=true" \
    -backend-config="encrypt=true"
}

tf_apply() {
  log_info "terraform apply ($$1)"
  terraform -chdir="$$1" apply -input=false -auto-approve
}

tf_destroy() {
  log_info "terraform destroy ($$1)"
  terraform -chdir="$$1" destroy -input=false -auto-approve
}

# Deletes all object versions and delete markers from a versioned S3 bucket
empty_versioned_bucket() {
  local bucket="$$1" region="$$2"
  log_warn "Emptying versioned bucket: $$bucket (region: $$region)"
  for query in \
    'Versions[].{Key:Key,VersionId:VersionId}' \
    'DeleteMarkers[].{Key:Key,VersionId:VersionId}'
  do
    while true; do
      local objects
      objects=$$(aws s3api list-object-versions \
        --bucket "$$bucket" --region "$$region" \
        --query "{Objects: $$query}" --output json 2>/dev/null \
        || printf '{"Objects":[]}')
      local count
      count=$$(printf '%s' "$$objects" \
        | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('Objects') or []))")
      [[ "$$count" -eq 0 ]] && break
      log_info "Deleting $$count object(s)..."
      printf '%s' "$$objects" | aws s3api delete-objects \
        --bucket "$$bucket" --region "$$region" --delete "$$(cat)" >/dev/null
    done
  done
  log_ok "Bucket emptied (all versions and delete markers removed)"
}
endef
export SHELL_HELPERS

# ══════════════════════════════════════════════════════════════════════════════
# Help
# ══════════════════════════════════════════════════════════════════════════════
help:
	@printf '\nAtlas — Makefile for AWS\n\n'
	@printf 'Install targets\n'
	@printf '  make install              Full install (bootstrap → cluster → repository → build → push → helm)\n'
	@printf '  make bootstrap            Step 1: Terraform bootstrap (S3 remote-state bucket)\n'
	@printf '  make cluster              Step 2: Terraform for EKS cluster creation\n'
	@printf '  make repository           Step 3: Terraform for ECR image repositories creation\n'
	@printf '  make build                Step 4a: Build jars and Docker images locally\n'
	@printf '  make push                 Step 4b: Push Docker images to ECR\n'
	@printf '  make helm                 Step 5: Helm install / upgrade (all subcharts)\n'
	@printf '  make helm-common          Step 5a: Helm install / upgrade — common (RBAC)\n'
	@printf '  make helm-infra           Step 5b: Helm install / upgrade — infra (databases, queues)\n'
	@printf '  make helm-security        Step 5c: Helm install / upgrade — security (Keycloak)\n'
	@printf '  make helm-observability   Step 5d: Helm install / upgrade — observability (metrics, logs)\n'
	@printf '  make helm-app             Step 5e: Helm install / upgrade — app services\n'
	@printf '\nUninstall targets\n'
	@printf '  make uninstall            Full 4-step uninstall (helm → repository → cluster → bootstrap)\n'
	@printf '  make destroy-helm         Step 1: Helm uninstall\n'
	@printf '  make destroy-repository   Step 2: Destroy ECR repositories\n'
	@printf '  make destroy-cluster      Step 3: Destroy EKS cluster\n'
	@printf '  make destroy-bootstrap    Step 4: Destroy S3 remote-state bucket\n'
	@printf '\nVariables (override on command line)\n'
	@printf '  HELM_RELEASE_NAME     Helm release name             (default: %s)\n' "$(HELM_RELEASE_NAME)"
	@printf '  HELM_NS_INFRA         Namespace for infra           (default: %s)\n' "$(HELM_NS_INFRA)"
	@printf '  HELM_NS_SECURITY      Namespace for security        (default: %s)\n' "$(HELM_NS_SECURITY)"
	@printf '  HELM_NS_OBSERVABILITY Namespace for observability   (default: %s)\n' "$(HELM_NS_OBSERVABILITY)"
	@printf '  HELM_NS_APP           Namespace for app services    (default: %s)\n\n' "$(HELM_NS_APP)"

# ══════════════════════════════════════════════════════════════════════════════
# Prerequisite check
# ══════════════════════════════════════════════════════════════════════════════
check-prereqs:
	@eval "$$SHELL_HELPERS"
	printf '\n===> Checking prerequisites\n'
	missing=0
	for tool in terraform aws helm kubectl; do
	  if command -v "$$tool" &>/dev/null; then
	    log_ok "$$tool found"
	  else
	    log_error "$$tool is not installed or not on PATH"
	    missing=$$((missing + 1))
	  fi
	done
	if [[ $$missing -gt 0 ]]; then
	  log_error "Please install the missing tools and re-run."
	  exit 1
	fi

# ══════════════════════════════════════════════════════════════════════════════
# INSTALL — full 4-step provisioning
# ══════════════════════════════════════════════════════════════════════════════
install: bootstrap cluster repository build push helm

# ══════════════════════════════════════════════════════════════════════════════
# UNINSTALL — full 4-step teardown (reverse order)
# ══════════════════════════════════════════════════════════════════════════════
uninstall: destroy-helm destroy-repository destroy-cluster destroy-bootstrap

# ══════════════════════════════════════════════════════════════════════════════
# Individual install steps
# ══════════════════════════════════════════════════════════════════════════════

## Step 1: Bootstrap — creates S3 bucket for Terraform remote state
bootstrap: check-prereqs
	@eval "$$SHELL_HELPERS"
	mkdir -p "$(STATE_DIR)"
	printf '\n===> Step 1/5 — Terraform bootstrap (remote state backend)\n'
	require_tfvars "$(TF_BOOTSTRAP_DIR)"
	log_info "terraform init (bootstrap)"
	terraform -chdir="$(TF_BOOTSTRAP_DIR)" init -input=false
	tf_apply "$(TF_BOOTSTRAP_DIR)"
	TF_BOOTSTRAP_BUCKET=$$(terraform -chdir="$(TF_BOOTSTRAP_DIR)" output -raw state_bucket_name)
	TF_BOOTSTRAP_REGION=$$(terraform -chdir="$(TF_BOOTSTRAP_DIR)" output -raw state_bucket_region)
	printf '%s' "$$TF_BOOTSTRAP_BUCKET" > "$(STATE_DIR)/bootstrap_bucket"
	printf '%s' "$$TF_BOOTSTRAP_REGION" > "$(STATE_DIR)/bootstrap_region"
	log_ok "Bootstrap complete — bucket: $$TF_BOOTSTRAP_BUCKET"

## Step 2: Creates EKS cluster (requires bootstrap state)
cluster: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_bootstrap_state
	printf '\n===> Step 2/5 — Terraform cluster (EKS)\n'
	require_tfvars "$(TF_CLUSTER_DIR)"
	tf_init "$(TF_CLUSTER_DIR)" "eks"
	tf_apply "$(TF_CLUSTER_DIR)"
	TF_CLUSTER_NAME=$$(terraform -chdir="$(TF_CLUSTER_DIR)" output -raw cluster_name)
	TF_CONFIGURE_KUBECTL=$$(terraform -chdir="$(TF_CLUSTER_DIR)" output -raw configure_kubectl)
	printf '%s' "$$TF_CLUSTER_NAME"      > "$(STATE_DIR)/cluster_name"
	printf '%s' "$$TF_CONFIGURE_KUBECTL" > "$(STATE_DIR)/configure_kubectl"
	log_ok "Cluster ready — name: $$TF_CLUSTER_NAME"
	configure_kubectl

## Step 3: Creates ECR image repositories (requires bootstrap state)
repository: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_bootstrap_state
	printf '\n===> Step 3/5 — Terraform repository (ECR)\n'
	require_tfvars "$(TF_REPO_DIR)"
	tf_init "$(TF_REPO_DIR)" "ecr"
	tf_apply "$(TF_REPO_DIR)"
	log_ok "ECR repositories created"

## Step 4a: Build — compile jars and build Docker images locally
build:
	@eval "$$SHELL_HELPERS"
	mkdir -p "$(STATE_DIR)"
	printf '\n===> Step 4a — Build jars and Docker images\n'
	IMAGE_TAG=$$(git -C "$(MAKEFILE_DIR)/.." rev-parse --short HEAD 2>/dev/null || printf 'latest')
	log_info "Image tag: $$IMAGE_TAG"

	declare -A BUILD_CONTEXT=(
	  [api-gateway]="$(MAKEFILE_DIR)/app/edge/api-gateway/api-gateway-reactive"
	  [authorization-server]="$(MAKEFILE_DIR)/app/edge/authorization-server"
	  [catalog-service]="$(MAKEFILE_DIR)/app/services/catalog-service/catalog-bootstrap"
	  [inventory-service]="$(MAKEFILE_DIR)/app/services/inventory-service/inventory-bootstrap"
	  [order-service]="$(MAKEFILE_DIR)/app/services/order-service/order-bootstrap"
	  [payment-service]="$(MAKEFILE_DIR)/app/services/payment-service/payment-bootstrap"
	  [user-service]="$(MAKEFILE_DIR)/app/services/user-service/user-bootstrap"
	)

	log_info "Building jars"
	"$(MAKEFILE_DIR)/app/gradlew" --project-dir "$(MAKEFILE_DIR)/app" build -x test
	log_ok "Jars built"

	for svc in "$${!BUILD_CONTEXT[@]}"; do
	  build_context="$${BUILD_CONTEXT[$$svc]}"
	  if [[ ! -d "$$build_context" ]]; then
	    log_warn "Build context not found for '$$svc' — skipping"
	    continue
	  fi
	  log_info "Building Docker image for '$$svc'"
	  docker build --platform linux/amd64 \
	    -t "$$svc:$$IMAGE_TAG" \
	    -t "$$svc:latest" \
	    "$$build_context"
	  log_ok "'$$svc' image built"
	done

	printf '%s' "$$IMAGE_TAG" > "$(STATE_DIR)/image_tag"
	log_ok "All images built (tag: $$IMAGE_TAG)"

## Step 4b: Push — push Docker images to ECR (requires bootstrap + build state)
push: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_bootstrap_state
	printf '\n===> Step 4b — Push Docker images to ECR\n'
	require_tfvars "$(TF_REPO_DIR)"
	load_build_state
	log_info "Image tag: $$IMAGE_TAG"

	log_info "Fetching ECR repository URLs"
	REPO_JSON=$$(terraform -chdir="$(TF_REPO_DIR)" output -json repository_urls)

	# Extract registry endpoint directly from the first repository URL
	ECR_REGISTRY=$$(printf '%s' "$$REPO_JSON" \
	  | python3 -c "import sys,json; d=json.load(sys.stdin); print(next(iter(d.values())).split('/')[0])")

	log_info "Logging in to ECR (registry: $$ECR_REGISTRY)"
	aws ecr get-login-password --region "$$TF_BOOTSTRAP_REGION" \
	  | docker login --username AWS --password-stdin "$$ECR_REGISTRY"

	declare -A REPO_URLS
	while IFS='=' read -r svc url; do
	  REPO_URLS["$$svc"]="$$url"
	done < <(printf '%s' "$$REPO_JSON" \
	  | python3 -c "import sys,json; [print(f'{k}={v}') for k,v in json.load(sys.stdin).items()]")

	for svc in "$${!REPO_URLS[@]}"; do
	  repo_url="$${REPO_URLS[$$svc]}"
	  if ! docker image inspect "$$svc:$$IMAGE_TAG" &>/dev/null; then
	    log_warn "Local image '$$svc:$$IMAGE_TAG' not found — run 'make build' first — skipping"
	    continue
	  fi
	  log_info "Tagging and pushing '$$svc' → $$repo_url"
	  docker tag "$$svc:$$IMAGE_TAG" "$$repo_url:$$IMAGE_TAG"
	  docker tag "$$svc:latest"      "$$repo_url:latest"
	  docker push "$$repo_url:$$IMAGE_TAG"
	  docker push "$$repo_url:latest"
	  log_ok "'$$svc' pushed successfully"
	done

	log_ok "All images pushed (tag: $$IMAGE_TAG)"
	printf '%s' "$$ECR_REGISTRY" > "$(STATE_DIR)/ecr_registry"
	log_ok "ECR registry saved to state"

## Step 5: Helm — deploys all subcharts (common → infra → security → observability → app)
helm: helm-common helm-infra helm-security helm-observability helm-app

## Step 5a: Helm — deploys common resources
helm-common: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_kubectl_state
	configure_kubectl
	printf '\n===> Step 5a/5 — Helm install/upgrade release '$(HELM_RELEASE_NAME)' (common)\n'
	helm upgrade --install "$(HELM_RELEASE_NAME)" "$(HELM_SUBCHART_DIR)/common" \
	  --values "$(HELM_CHART_DIR)/values.yaml" \
	  --debug
	log_ok "Helm release '$(HELM_RELEASE_NAME)' (common) deployed successfully"

## Step 5b: Helm — deploys infrastructure (databases, message queues, object storage)
helm-infra: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_kubectl_state
	configure_kubectl
	printf '\n===> Step 5b/5 — Helm install/upgrade release '$(HELM_RELEASE_NAME)' (infra)\n'
	helm upgrade --install "$(HELM_RELEASE_NAME)" "$(HELM_SUBCHART_DIR)/infra" \
	  --namespace "$(HELM_NS_INFRA)" \
	  --create-namespace \
	  --values "$(HELM_CHART_DIR)/values.yaml" \
	  --debug
	log_ok "Helm release '$(HELM_RELEASE_NAME)' (infra) deployed successfully"

## Step 5c: Helm — deploys security layer (Keycloak)
helm-security: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_kubectl_state
	configure_kubectl
	printf '\n===> Step 5c/5 — Helm install/upgrade release '$(HELM_RELEASE_NAME)' (security)\n'
	helm upgrade --install "$(HELM_RELEASE_NAME)" "$(HELM_SUBCHART_DIR)/security" \
	  --namespace "$(HELM_NS_SECURITY)" \
	  --create-namespace \
	  --values "$(HELM_CHART_DIR)/values.yaml" \
	  --debug
	log_ok "Helm release '$(HELM_RELEASE_NAME)' (security) deployed successfully"

## Step 5d: Helm — deploys observability stack (Prometheus, Grafana, Loki, Tempo)
helm-observability: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_kubectl_state
	configure_kubectl
	printf '\n===> Step 5d/5 — Helm install/upgrade release '$(HELM_RELEASE_NAME)' (observability)\n'
	helm upgrade --install "$(HELM_RELEASE_NAME)" "$(HELM_SUBCHART_DIR)/observability" \
	  --namespace "$(HELM_NS_OBSERVABILITY)" \
	  --create-namespace \
	  --values "$(HELM_CHART_DIR)/values.yaml" \
	  --debug
	log_ok "Helm release '$(HELM_RELEASE_NAME)' (observability) deployed successfully"

## Step 5e: Helm — deploys application services (requires cluster + push state)
helm-app: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_kubectl_state
	load_push_state
	configure_kubectl
	printf '\n===> Step 5e/5 — Helm install/upgrade release '$(HELM_RELEASE_NAME)' (app)\n'
		log_info "ECR registry: $$ECR_REGISTRY"
		log_info "Image tag:    $$IMAGE_TAG"
	helm upgrade --install "$(HELM_RELEASE_NAME)" "$(HELM_SUBCHART_DIR)/app" \
	  --namespace "$(HELM_NS_APP)" \
	  --create-namespace \
	  --values "$(HELM_CHART_DIR)/values.yaml" \
	  --set global.image.registry="$$ECR_REGISTRY" \
	  --set global.image.tag="$$IMAGE_TAG" \
	  --set global.image.pullPolicy=Always \
	  --debug
	log_ok "Helm release '$(HELM_RELEASE_NAME)' (app) deployed successfully"

# ══════════════════════════════════════════════════════════════════════════════
# Individual uninstall steps
# ══════════════════════════════════════════════════════════════════════════════

## Step 1 (uninstall): Helm uninstall (all subcharts in reverse order)
destroy-helm:
	@eval "$$SHELL_HELPERS"
	if [[ -f "$(STATE_DIR)/configure_kubectl" ]]; then
	  TF_CONFIGURE_KUBECTL=$$(cat "$(STATE_DIR)/configure_kubectl")
	  configure_kubectl
	else
	  log_warn "configure_kubectl state not found — skipping kubectl context update"
	fi
	printf '\n===> Step 1/4 — Helm uninstall ($(HELM_RELEASE_NAME)-*)\n'
	uninstall_release() {
	  local release="$$1" ns="$$2"
	  if ! helm status "$$release" --namespace "$$ns" &>/dev/null; then
	    log_warn "Helm release '$$release' not found in namespace '$$ns' — skipping"
	  else
	    log_info "helm uninstall $$release (namespace: $$ns)"
	    helm uninstall "$$release" --namespace "$$ns"
	    log_ok "Helm release '$$release' removed"
	  fi
	  if kubectl get namespace "$$ns" &>/dev/null; then
	    log_info "Deleting namespace '$$ns'"
	    kubectl delete namespace "$$ns" --wait=true
	    log_ok "Namespace '$$ns' deleted"
	  fi
	}
	uninstall_release "$(HELM_RELEASE_NAME)" "$(HELM_NS_APP)"
	uninstall_release "$(HELM_RELEASE_NAME)" "$(HELM_NS_OBSERVABILITY)"
	uninstall_release "$(HELM_RELEASE_NAME)" "$(HELM_NS_SECURITY)"
	uninstall_release "$(HELM_RELEASE_NAME)" "$(HELM_NS_INFRA)"
	if helm status "$(HELM_RELEASE_NAME)" &>/dev/null; then
	  log_info "helm uninstall $(HELM_RELEASE_NAME)"
	  helm uninstall "$(HELM_RELEASE_NAME)"
	  log_ok "Helm release '$(HELM_RELEASE_NAME)' removed"
	else
	  log_warn "Helm release '$(HELM_RELEASE_NAME)' not found — skipping"
	fi

## Step 2 (uninstall): Destroy ECR repositories (requires bootstrap state)
destroy-repository: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_bootstrap_state
	printf '\n===> Step 2/4 — Terraform destroy (ECR repositories)\n'
	require_tfvars "$(TF_REPO_DIR)"
	tf_init "$(TF_REPO_DIR)" "ecr"
	tf_destroy "$(TF_REPO_DIR)"
	log_ok "ECR repositories destroyed"

## Step 3 (uninstall): Destroy EKS cluster (requires bootstrap state)
destroy-cluster: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_bootstrap_state
	printf '\n===> Step 3/4 — Terraform destroy (EKS cluster)\n'
	require_tfvars "$(TF_CLUSTER_DIR)"
	tf_init "$(TF_CLUSTER_DIR)" "eks"
	tf_destroy "$(TF_CLUSTER_DIR)"
	log_ok "EKS cluster destroyed"

## Step 4 (uninstall): Destroy S3 remote-state bucket (bootstrap)
destroy-bootstrap: check-prereqs
	@eval "$$SHELL_HELPERS"
	load_bootstrap_state
	printf '\n===> Step 4/4 — Terraform destroy (bootstrap)\n'
	require_tfvars "$(TF_BOOTSTRAP_DIR)"
	log_info "terraform init (bootstrap)"
	terraform -chdir="$(TF_BOOTSTRAP_DIR)" init -input=false
	empty_versioned_bucket "$$TF_BOOTSTRAP_BUCKET" "$$TF_BOOTSTRAP_REGION"
	tf_destroy "$(TF_BOOTSTRAP_DIR)"
	rm -f "$(STATE_DIR)/bootstrap_bucket" "$(STATE_DIR)/bootstrap_region" \
	      "$(STATE_DIR)/cluster_name"     "$(STATE_DIR)/configure_kubectl" \
	      "$(STATE_DIR)/ecr_registry"     "$(STATE_DIR)/image_tag"
	log_ok "Bootstrap resources destroyed"
