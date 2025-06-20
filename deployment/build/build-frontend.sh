#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Load logger
source "$PROJECT_ROOT/deployment/utils/logger.sh"

log_info "Building frontend..."
cd "$PROJECT_ROOT/frontend"

# Install dependencies
log_info "Installing npm dependencies..."
npm install

# Build the frontend
log_info "Building frontend application..."
npm run build

log_success "Frontend build completed successfully."
