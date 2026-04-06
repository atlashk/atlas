#!/usr/bin/env bash
# ==============================================================
# install.eks.sh
#
# Provisions the full Atlas stack on AWS EKS:
#   1. terraform/bootstrap — creates the S3 bucket + DynamoDB
#                            table for Terraform remote state
#   2. terraform/cluster   — creates the EKS cluster + VPC +
#                            IAM roles + node groups
#   3. helm install/upgrade — deploys all Kubernetes resources
#                             onto the cluster
#
# Usage:
#   chmod +x install.eks.sh
#   ./install.eks.sh
#
# Prerequisites:
#   - terraform >= 1.9
#   - aws-cli   >= 2  (configured with appropriate credentials)
#   - helm      >= 3
#   - kubectl
#   - Both terraform.tfvars files must already exist:
#       terraform/eks/bootstrap/terraform.tfvars
#       terraform/eks/cluster/terraform.tfvars
# ==============================================================

set -euo pipefail

# ---------------------------------------------------------------
# Colour helpers
# ---------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

log_info()    { echo -e "${CYAN}[INFO]${RESET}  $*"; }
log_success() { echo -e "${GREEN}[OK]${RESET}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${RESET}  $*"; }
log_error()   { echo -e "${RED}[ERROR]${RESET} $*" >&2; }
log_step()    { echo -e "\n${BOLD}${CYAN}===> $*${RESET}"; }

# ---------------------------------------------------------------
# Resolve directories relative to this script
# ---------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_BOOTSTRAP_DIR="${SCRIPT_DIR}/terraform/eks/bootstrap"
TF_CLUSTER_DIR="${SCRIPT_DIR}/terraform/eks/cluster"
HELM_CHART_DIR="${SCRIPT_DIR}/helm"

HELM_RELEASE_NAME="atlas"
HELM_NAMESPACE="atlas"

# ---------------------------------------------------------------
# Helper: check required tools
# ---------------------------------------------------------------
check_prerequisites() {
    log_step "Checking prerequisites"

    local missing=0
    for tool in terraform aws helm kubectl; do
        if command -v "$tool" &>/dev/null; then
            log_success "$tool found ($(${tool} version --short 2>/dev/null | head -1 || true))"
        else
            log_error "$tool is not installed or not on PATH"
            missing=$((missing + 1))
        fi
    done

    if [[ $missing -gt 0 ]]; then
        log_error "Please install the missing tools and re-run the script."
        exit 1
    fi
}

# ---------------------------------------------------------------
# Helper: assert that terraform.tfvars exists in a directory
# ---------------------------------------------------------------
assert_tfvars() {
    local dir="$1"
    if [[ ! -f "${dir}/terraform.tfvars" ]]; then
        log_error "terraform.tfvars not found in ${dir}"
        log_error "Copy the example file and fill in your values:"
        log_error "  cp ${dir}/terraform.tfvars.example ${dir}/terraform.tfvars"
        exit 1
    fi
}

# ---------------------------------------------------------------
# Step 1 — Bootstrap: S3 bucket + DynamoDB table
# ---------------------------------------------------------------
run_bootstrap() {
    log_step "Step 1/3 — Terraform bootstrap (remote state backend)"

    assert_tfvars "${TF_BOOTSTRAP_DIR}"

    pushd "${TF_BOOTSTRAP_DIR}" > /dev/null

    log_info "terraform init"
    terraform init -input=false

    log_info "terraform apply"
    terraform apply -input=false -auto-approve

    # Capture outputs for downstream use
    TF_BOOTSTRAP_BUCKET=$(terraform output -raw state_bucket_name)
    TF_BOOTSTRAP_TABLE=$(terraform output -raw lock_table_name)
    TF_BOOTSTRAP_REGION=$(terraform output -raw state_bucket_region)

    popd > /dev/null

    log_success "Bootstrap complete — bucket: ${TF_BOOTSTRAP_BUCKET}, table: ${TF_BOOTSTRAP_TABLE}"
}

# ---------------------------------------------------------------
# Step 2 — Cluster: EKS + VPC + node groups
# ---------------------------------------------------------------
run_cluster() {
    log_step "Step 2/3 — Terraform cluster (EKS)"

    assert_tfvars "${TF_CLUSTER_DIR}"

    pushd "${TF_CLUSTER_DIR}" > /dev/null

    log_info "terraform init"
    terraform init -input=false \
        -backend-config="bucket=${TF_BOOTSTRAP_BUCKET}" \
        -backend-config="key=${HELM_RELEASE_NAME}/eks/terraform.tfstate" \
        -backend-config="region=${TF_BOOTSTRAP_REGION}" \
        -backend-config="dynamodb_table=${TF_BOOTSTRAP_TABLE}" \
        -backend-config="encrypt=true"

    log_info "terraform apply"
    terraform apply -input=false -auto-approve

    # Capture cluster outputs
    TF_CLUSTER_REGION=$(terraform output -raw -no-color \
        2>/dev/null <<< "$(terraform output -json summary)" | \
        python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('region',''))" 2>/dev/null || \
        terraform output -raw aws_region 2>/dev/null || \
        echo "us-east-1")

    TF_CLUSTER_NAME=$(terraform output -raw cluster_name)
    TF_CONFIGURE_KUBECTL=$(terraform output -raw configure_kubectl)

    popd > /dev/null

    log_success "Cluster ready — name: ${TF_CLUSTER_NAME}"
}

# ---------------------------------------------------------------
# Step 2b — Configure kubectl
# ---------------------------------------------------------------
configure_kubectl() {
    log_step "Configuring kubectl"
    log_info "Running: ${TF_CONFIGURE_KUBECTL}"
    eval "${TF_CONFIGURE_KUBECTL}"
    log_success "kubectl context updated to cluster: ${TF_CLUSTER_NAME}"
}

# ---------------------------------------------------------------
# Step 3 — Helm install / upgrade
# ---------------------------------------------------------------
run_helm() {
    log_step "Step 3/3 — Helm install/upgrade (${HELM_RELEASE_NAME})"

    # Create namespace if it does not exist
    if ! kubectl get namespace "${HELM_NAMESPACE}" &>/dev/null; then
        log_info "Creating namespace '${HELM_NAMESPACE}'"
        kubectl create namespace "${HELM_NAMESPACE}"
    fi

    log_info "helm upgrade --install ${HELM_RELEASE_NAME}"
    helm upgrade --install "${HELM_RELEASE_NAME}" "${HELM_CHART_DIR}" \
        --namespace "${HELM_NAMESPACE}" \
        --create-namespace \
        --values "${HELM_CHART_DIR}/values.yaml" \
        --wait \
        --timeout 30m

    log_success "Helm release '${HELM_RELEASE_NAME}' deployed successfully"
}

# ---------------------------------------------------------------
# Main
# ---------------------------------------------------------------
main() {
    echo -e "${BOLD}${CYAN}"
    echo "=============================================================="
    echo "  Atlas — EKS Full-Stack Installer"
    echo "=============================================================="
    echo -e "${RESET}"

    check_prerequisites
    run_bootstrap
    run_cluster
    configure_kubectl
    run_helm

    echo -e "\n${BOLD}${GREEN}"
    echo "=============================================================="
    echo "  Installation complete!"
    echo "  Cluster : ${TF_CLUSTER_NAME}"
    echo "  Release : ${HELM_RELEASE_NAME} (namespace: ${HELM_NAMESPACE})"
    echo "=============================================================="
    echo -e "${RESET}"
}

main "$@"
