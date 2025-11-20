#!/usr/bin/env bash
set -euo pipefail

# -------------------------------------------------------------
# Atlas On-Premise Setup Script
# -------------------------------------------------------------
# Primary goals:
#  1) Create `backend/app-stack.cfg` from the platform default config
#     or via an interactive wizard for manual stack selection.
#  2) Render deployment files (Docker Compose/Kubernetes Native)
#     from EJS templates into `backend/scripts/deployment/generated`.
#
# Notes:
#  - This script uses arrow keys ↑↓ and Enter for terminal interaction.
#  - Requires Node.js; will install `ejs` if missing (via npm).
#  - On Windows, recommended to run in Git Bash or WSL.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ROOT_DIR="$(cd "$BACKEND_DIR/.." && pwd)"

APP_STACK_FILE="$BACKEND_DIR/app-stack.cfg"              # Stack configuration used for template rendering
TEMPLATE_GENERATOR="$BACKEND_DIR/scripts/template-generator.mjs" # EJS rendering utility
COMPOSE_TEMPLATES_DIR="$BACKEND_DIR/scripts/deployment/templates/onprem/compose" # Templates for Docker Compose
K8S_NATIVE_TEMPLATES_DIR="$BACKEND_DIR/scripts/deployment/templates/onprem/k8s/native" # Templates for Kubernetes native
DEPLOYMENT_GENERATED_DIR="$BACKEND_DIR/scripts/deployment/generated" # Render output directory
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'
BOLD='\033[1m'

info() { printf "[INFO] %s\n" "$*"; }   # Print info message
warn() { printf "[WARN] %s\n" "$*"; }   # Print warning message
err()  { printf "[ERROR] %s\n" "$*"; }  # Print error message

confirm_use_stack() {
  # Ask whether to use the default config file to create app-stack.cfg
  local file="$1"
  printf "Use %s to generate app-stack.cfg? [Y/n] " "$(basename "$file")"
  read -r answer
  answer=${answer:-Y}
  case "$answer" in
    Y|y|Yes|yes)
      return 0 ;;
    *)
      return 1 ;;
  esac
}

show_stack_file() {
  # Display content of the default config file if it exists
  local file="$1"
  if [[ -f "$file" ]]; then
    info "Found configuration: $file"
    cat "$file"
    echo "---------------------------------"
  else
    warn "Configuration not found: $file"
  fi
}

select_option() {
  # Option selector using ↑↓ and Enter keys
  # Note: Temporarily disable `set -e` to avoid exiting on benign `read` errors
  set +e
  local options=("$@")
  local max_index=${#options[@]}
  local selected=1
  if [ $max_index -eq 1 ]; then
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) ${options[0]}${NC}"
    echo
    sleep 1
    SELECTED_INDEX=1
    set -e
    return 0
  fi
  display_menu() {
    for ((i=0; i<${#options[@]}; i++)); do
      if [ $((i+1)) -eq $selected ]; then
        printf "\r  ${CYAN}▶ ${GREEN}%d${NC}) " "$((i+1))"
        echo -e "${options[$i]}${NC}"
      else
        printf "\r    ${GREEN}%d${NC}) " "$((i+1))"
        echo -e "${options[$i]}"
      fi
    done
  }
  display_menu
  while true; do
    IFS= read -rsn1 key
    case $key in
      $'\e')
        read -rsn2 key
        case $key in
          '[A')
            if [ $selected -gt 1 ]; then selected=$((selected-1)); fi ;;
          '[B')
            if [ $selected -lt $max_index ]; then selected=$((selected+1)); fi ;;
        esac
        for ((i=0; i<max_index; i++)); do printf "\033[1A\033[K"; done
        display_menu ;;
      '')
        break ;;
      [1-9])
        if [ "$key" -le "$max_index" ]; then
          selected=$key
          for ((i=0; i<max_index; i++)); do printf "\033[1A\033[K"; done
          display_menu
        fi ;;
    esac
  done
  echo
  sleep 1
  SELECTED_INDEX=$selected
  set -e
  return 0
}

run_custom_stack_interactive() {
  # Interactive wizard to collect selections for each stack component
  local platform_chosen="$1"
  echo -e "${CYAN}${BOLD}Atlas Stack Configuration${NC}"
  echo -e "${CYAN}Use arrow keys ↑↓ to navigate, Enter to select:${NC}"
  echo
  local platform
  platform="$platform_chosen"

  # API Server
  echo -e "${BLUE}${BOLD}API Server Configuration${NC}"
  select_option "REST ${YELLOW}${NC}" "gRPC"
  local api_server_choice=$SELECTED_INDEX
  local api_server
  case $api_server_choice in
    2) api_server="grpc" ;;
    *) api_server="rest" ;;
  esac

  # API Client
  echo -e "${BLUE}${BOLD}API Client Configuration${NC}"
  local api_client
  if [ "$api_server" = "grpc" ]; then
    api_client="grpc-netdevh"
  else
    select_option "RestClient ${YELLOW}${NC}" "Apache HttpClient" "Feign" "RestTemplate"
    local api_client_choice=$SELECTED_INDEX
    case $api_client_choice in
      2) api_client="rest-apachehttpclient" ;;
      3) api_client="rest-feign" ;;
      4) api_client="rest-resttemplate" ;;
      *) api_client="rest-restclient" ;;
    esac
  fi

  # Datasource
  echo -e "${BLUE}${BOLD}Datasource Configuration${NC}"
  select_option "MySQL ${YELLOW}${NC}" "PostgreSQL"
  local datasource_choice=$SELECTED_INDEX
  local datasource
  case $datasource_choice in
    2) datasource="postgresql" ;;
    *) datasource="mysql" ;;
  esac

  # Discovery Client
  local discovery_client
  if [[ "$platform" == onprem-k8s* ]]; then
    discovery_client="kubernetes"
  else
    discovery_client="eureka"
  fi

  # File - CSV
  local file_csv="opencsv"

  # File - Excel
  echo -e "${BLUE}${BOLD}File - Excel Configuration${NC}"
  select_option "Apache POI ${YELLOW}${NC}" "EasyExcel"
  local file_excel_choice=$SELECTED_INDEX
  local file_excel
  case $file_excel_choice in
    2) file_excel="easyexcel" ;;
    *) file_excel="poi" ;;
  esac

  # File - PDF
  local file_pdf="pdfbox"

  # Key-Value Store
  local kv_store="redis"

  # Redis Deployment
  local redis="standalone"

  # Messaging
  echo -e "${BLUE}${BOLD}Messaging Configuration${NC}"
  select_option "Apache Kafka ${YELLOW}${NC}" "RabbitMQ"
  local messaging_choice=$SELECTED_INDEX
  local messaging
  case $messaging_choice in
    2) messaging="rabbitmq" ;;
    *) messaging="kafka" ;;
  esac

  # Migration
  local migration="flyway"

  # Notification - Email
  echo -e "${BLUE}${BOLD}Email Notification Configuration${NC}"
  select_option "Spring ${YELLOW}${NC}" "SendGrid"
  local email_choice=$SELECTED_INDEX
  local notification_email
  case $email_choice in
    2) notification_email="sendgrid" ;;
    *) notification_email="spring" ;;
  esac

  # Notification - In-App
  echo -e "${BLUE}${BOLD}In-App Notification Configuration${NC}"
  select_option "Server-Sent Events ${YELLOW}${NC}" "WebSocket"
  local notification_inapp_choice=$SELECTED_INDEX
  local notification_inapp
  case $notification_inapp_choice in
    2) notification_inapp="websocket" ;;
    *) notification_inapp="sse" ;;
  esac

  # Observability Logging Stack
  echo -e "${BLUE}${BOLD}Observability Logging Stack Configuration${NC}"
  select_option "Loki ${YELLOW}${NC}" "None"
  local observability_logging_stack_choice=$SELECTED_INDEX
  local observability_logging_stack
  case $observability_logging_stack_choice in
    2) observability_logging_stack="none" ;;
    *) observability_logging_stack="loki" ;;
  esac

  # Observability Logging Framework
  local observability_logging_framework="logback"

  # Observability Metrics
  echo -e "${BLUE}${BOLD}Observability Metrics Configuration${NC}"
  select_option "Prometheus ${YELLOW}${NC}" "None"
  local observability_metrics_choice=$SELECTED_INDEX
  local observability_metrics
  case $observability_metrics_choice in
    2) observability_metrics="none" ;;
    *) observability_metrics="prometheus" ;;
  esac

  # Observability Tracing
  echo -e "${BLUE}${BOLD}Observability Tracing Configuration${NC}"
  select_option "Zipkin ${YELLOW}${NC}" "None"
  local observability_tracing_choice=$SELECTED_INDEX
  local observability_tracing
  case $observability_tracing_choice in
    2) observability_tracing="none" ;;
    *) observability_tracing="zipkin" ;;
  esac

  # Persistence
  local persistence="jpa"

  # Reverse Proxy
  echo -e "${BLUE}${BOLD}Reverse Proxy Configuration${NC}"
  local reverse_proxy
  if [[ "$platform" == onprem-k8s* ]]; then
    reverse_proxy="nginx"
  else
    select_option "Nginx ${YELLOW}${NC}" "None"
    local reverse_proxy_choice=$SELECTED_INDEX
    case $reverse_proxy_choice in
      2) reverse_proxy="none" ;;
      *) reverse_proxy="nginx" ;;
    esac
  fi

  # Scheduler
  echo -e "${BLUE}${BOLD}Scheduler Configuration${NC}"
  select_option "Quartz ${YELLOW}${NC}" "Spring"
  local scheduler_choice=$SELECTED_INDEX
  local scheduler
  case $scheduler_choice in
    2) scheduler="spring" ;;
    *) scheduler="quartz" ;;
  esac

  # Search
  echo -e "${BLUE}${BOLD}Search Configuration${NC}"
  select_option "Database ${YELLOW}${NC}" "Elasticsearch"
  local search_choice=$SELECTED_INDEX
  local search
  case $search_choice in
    2) search="elasticsearch" ;;
    *) search="database" ;;
  esac

  # Storage
  echo -e "${BLUE}${BOLD}Storage Configuration${NC}"
  select_option "Filesystem ${YELLOW}${NC}" "MinIO"
  local storage_choice=$SELECTED_INDEX
  local storage
  case $storage_choice in
    2) storage="minio" ;;
    *) storage="filesystem" ;;
  esac

  # Template Engine
  echo -e "${BLUE}${BOLD}Template Engine Configuration${NC}"
  select_option "Freemarker ${YELLOW}${NC}" "Thymeleaf"
  local template_choice=$SELECTED_INDEX
  local template
  case $template_choice in
    2) template="thymeleaf" ;;
    *) template="freemarker" ;;
  esac

  echo -e "${PURPLE}${BOLD}Generating app-stack.cfg file...${NC}"
  sleep 2
  cat > "$APP_STACK_FILE" << EOF
platform=$platform
api-server=$api_server
api-client=$api_client
datasource=$datasource
discovery-client=$discovery_client
file.csv=$file_csv
file.excel=$file_excel
file.pdf=$file_pdf
kv-store=$kv_store
messaging=$messaging
migration=$migration
notification.email=$notification_email
notification.inapp=$notification_inapp
observability.logging.stack=$observability_logging_stack
observability.logging.framework=$observability_logging_framework
observability.metrics=$observability_metrics
observability.tracing=$observability_tracing
persistence=$persistence
redis=$redis
reverse-proxy=$reverse_proxy
scheduler=$scheduler
search=$search
storage=$storage
template=$template
EOF
  # Verify configuration file was created successfully
  if [ ! -f "$APP_STACK_FILE" ]; then
    echo -e "${RED}${BOLD}Error: Failed to create configuration file at ${APP_STACK_FILE}${NC}"
    echo -e "${RED}Please check if the directory exists and you have write permissions.${NC}"
    exit 1
  fi
}

# Ensure the 'ejs' package is available for template rendering; prompt to install if missing
ensure_template_deps() {
  # Ensure Node.js is available for template rendering
  if ! command -v node >/dev/null 2>&1; then
    err "Node.js is required to render templates. Please install Node.js."
    exit 1
  fi

  # Ensure 'ejs' package is available for EJS template rendering
  if node -e "try{require('ejs');}catch(e){process.exit(1)}" >/dev/null 2>&1; then
    return 0
  fi
  warn "Missing Node package 'ejs' required by template-generator.mjs."
  if ! command -v npm >/dev/null 2>&1; then
    err "npm is required to install dependencies. Please install npm (Node.js)."
    exit 1
  fi
  printf "Install 'ejs' in backend directory now? [Y/n] "
  read -r answer
  answer=${answer:-Y}
  case "$answer" in
    Y|y|Yes|yes)
      (
        cd "$BACKEND_DIR" || exit 1
        if [[ ! -f package.json ]]; then
          info "Initializing npm in $BACKEND_DIR"
          npm init -y >/dev/null 2>&1 || true
        fi
        info "Installing ejs in $BACKEND_DIR"
        npm install ejs --save || {
          err "Failed to install 'ejs'."
          exit 1
        }
      )
      ;;
    *)
      err "Cannot proceed without 'ejs'. Aborting."
      exit 1
      ;;
  esac
}

normalize_line_endings_to_lf() {
  # Normalize line endings to LF to avoid issues on Linux/WSL/Git Bash
  local dir="$1"
  if [[ -d "$dir" ]]; then
    if command -v dos2unix >/dev/null 2>&1; then
      find "$dir" -type f \( -name "*.sh" -o -name "*.sql" \) -exec dos2unix -q {} \;
    else
      while IFS= read -r -d '' f; do
        awk '{ sub(/\r$/, ""); print }' "$f" > "$f.tmp" && mv "$f.tmp" "$f"
      done < <(find "$dir" -type f \( -name "*.sh" -o -name "*.sql" \) -print0)
    fi
    find "$dir" -type f -name "*.sh" -exec chmod +x {} \;
  fi
}

read_platform() {
  # Read `platform` from app-stack.cfg; default to `onprem-compose`
  if [[ -f "$APP_STACK_FILE" ]]; then
    grep '^platform=' "$APP_STACK_FILE" | cut -d'=' -f2 | tr -d '[:space:]'
  else
    echo "onprem-compose"
  fi
}

render_templates() {
  # Render templates from source directory into the generated directory
  local src_dir="$1"
  ensure_template_deps
  if [[ ! -d "$src_dir" ]]; then
    err "Templates directory not found: $src_dir"
    exit 1
  fi
  info "Rendering templates from $src_dir to $DEPLOYMENT_GENERATED_DIR"
  node "$TEMPLATE_GENERATOR" --dir "$src_dir" --out-dir "$DEPLOYMENT_GENERATED_DIR" --cfg "$APP_STACK_FILE"
}

reset_deployment_generated_dir() {
  # Clean and recreate generated directory to ensure a fresh output
  if [[ -d "$DEPLOYMENT_GENERATED_DIR" ]]; then
    rm -rf "$DEPLOYMENT_GENERATED_DIR"
  fi
  mkdir -p "$DEPLOYMENT_GENERATED_DIR"
}

main() {
  # Main flow: 
  # 1. Choose platform
  # 2. Create app-stack.cfg,
  # 3. Render deployment files based on platform's templates
  # 4. Normalize line endings of deployment files to LF

  info "Atlas on-premise setup starting..."
  echo -e "${BLUE}${BOLD}Platform Configuration${NC}"
  select_option "On-Premise (Docker Compose) ${YELLOW}${NC}" "On-Premise (Kubernetes Native)"
  platform_choice=$SELECTED_INDEX
  case $platform_choice in
    2)
      selected_platform="onprem-k8s-native" ;;
    *)
      selected_platform="onprem-compose" ;;
  esac

  if [[ "$selected_platform" == "onprem-k8s-native" ]]; then
    PLATFORM_DEFAULT_STACK_FILE="$BACKEND_DIR/app-stack.onprem.k8s.native.cfg"
  else
    PLATFORM_DEFAULT_STACK_FILE="$BACKEND_DIR/app-stack.onprem.compose.cfg"
  fi

  show_stack_file "$PLATFORM_DEFAULT_STACK_FILE"
  if [[ -f "$PLATFORM_DEFAULT_STACK_FILE" ]] && confirm_use_stack "$PLATFORM_DEFAULT_STACK_FILE"; then
    sudo cp "$PLATFORM_DEFAULT_STACK_FILE" "$APP_STACK_FILE"
    info "app-stack.cfg generated from selected platform default config."
  else
    run_custom_stack_interactive "$selected_platform"
  fi

  reset_deployment_generated_dir
  local platform
  platform="$(read_platform)"
  case "$platform" in
    onprem-compose)
      render_templates "$COMPOSE_TEMPLATES_DIR" ;;
    onprem-k8s-native)
      render_templates "$K8S_NATIVE_TEMPLATES_DIR" ;;
    *)
      warn "Platform '$platform' is not yet supported by this simple setup."
      exit 1
      ;;
  esac

  normalize_line_endings_to_lf "$DEPLOYMENT_GENERATED_DIR"

  info "Setup completed."
  info "Next steps:"
  info "  - Review generated files in: $DEPLOYMENT_GENERATED_DIR"
  info "  - Run: $DEPLOYMENT_GENERATED_DIR/deploy.sh to start services"
  info "  - Run: $DEPLOYMENT_GENERATED_DIR/clean.sh to stop and cleanup"
}

main "$@"
