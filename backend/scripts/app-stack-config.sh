#!/bin/bash

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color
BOLD='\033[1m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Default values
APP_STACK_FILE="app-stack.cfg"

# Usage function
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --app-stack-file=FILE   App stack config file name (default: app-stack.cfg)"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Generate app-stack.cfg with interactive configuration"
    echo "  $0 --app-stack-file=app-stack.dev.cfg # Generate custom config file"
}

# Parse command line arguments
for arg in "$@"; do
    case $arg in
        --app-stack-file=*)
            APP_STACK_FILE="${arg#*=}"
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown parameter: $arg" >&2
            usage
            exit 1
            ;;
    esac
done

# Set the full path for the config file
APP_STACK_FILE_PATH="$PROJECT_ROOT/backend/$APP_STACK_FILE"

# Function to select option using arrow keys
select_option() {
    local options=("$@")
    local max_index=${#options[@]}
    local selected=1

    # If only one option, auto-select it
    if [ $max_index -eq 1 ]; then
        echo -e "  ${CYAN}▶ ${GREEN}1${NC}) ${options[0]}${NC}"
        echo
        sleep 2
        return 0
    fi

    # Function to display menu
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
    
    # Initial display
    display_menu
    
    # Handle input
    while true; do
        # Read single character
        IFS= read -rsn1 key
        
        case $key in
            $'\e')  # Escape sequence
                read -rsn2 key
                case $key in
                    '[A')  # Up arrow
                        if [ $selected -gt 1 ]; then
                            selected=$((selected-1))
                        fi
                        ;;
                    '[B')  # Down arrow
                        if [ $selected -lt $max_index ]; then
                            selected=$((selected+1))
                        fi
                        ;;
                esac
                
                # Clear and redraw
                for ((i=0; i<max_index; i++)); do
                    printf "\033[1A\033[K"
                done
                display_menu
                ;;
            '')  # Enter key
                break
                ;;
            [1-9])  # Number keys
                if [ "$key" -le "$max_index" ]; then
                    selected=$key
                    # Clear and redraw
                    for ((i=0; i<max_index; i++)); do
                        printf "\033[1A\033[K"
                    done
                    display_menu
                fi
                ;;
        esac
    done

    echo
    sleep 1
    return $((selected-1))
}

# Header
echo -e "${CYAN}${BOLD}Atlas Stack Configuration${NC}"
echo -e "${CYAN}Use arrow keys ↑↓ to navigate, Enter to select:${NC}"
echo

# =============================================================================
# PLATFORM CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Platform Configuration${NC}"
select_option "On-Premise (Docker Compose) ${YELLOW}(default)${NC}" "On-Premise (Kubernetes Native)"
platform_choice=$((1 + $?))
case $platform_choice in
    2)
        platform="onprem-k8s-native"
        ;;
    *)
        platform="onprem-compose"
        ;;
esac

# =============================================================================
# API SERVER CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}API Server Configuration${NC}"
select_option "REST ${YELLOW}(default)${NC}" "gRPC"
api_server_choice=$((1 + $?))
case $api_server_choice in
    2)
        api_server="grpc"
        ;;
    *)
        api_server="rest"
        ;;
esac

# =============================================================================
# API CLIENT CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}API Client Configuration${NC}"
if [ "$api_server" = "grpc" ]; then
    select_option "gRPC netdevh ${YELLOW}(default)${NC}"
    api_client_choice=$((1 + $?))
    api_client="grpc-netdevh"
else
    select_option "RestClient ${YELLOW}(default)${NC}" "Apache HttpClient" "Feign" "RestTemplate"
    api_client_choice=$((1 + $?))
    
    case $api_client_choice in
        2)
            api_client="rest-apachehttpclient"
            ;;
        3)
            api_client="rest-feign"
            ;;
        4)
            api_client="rest-resttemplate"
            ;;
        *)
            api_client="rest-restclient"
            ;;
    esac
fi
echo

# =============================================================================
# DATASOURCE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Datasource Configuration${NC}"
select_option "MySQL ${YELLOW}(default)${NC}" "PostgreSQL"
datasource_choice=$((1 + $?))
case $datasource_choice in
    2)
        datasource="postgresql"
        ;;
    *)
        datasource="mysql"
        ;;
esac

# =============================================================================
# DISCOVERY CLIENT CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Discovery Client Configuration${NC}"
if [[ "$platform" == onprem-k8s* ]]; then
    discovery_client="kubernetes"
    select_option "Kubernetes Built-in DNS ${YELLOW}(default)${NC}"
else
    discovery_client="eureka"
    select_option "Eureka ${YELLOW}(default)${NC}"
fi

# =============================================================================
# FILE - CSV CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File - CSV Configuration${NC}"
select_option "OpenCSV ${YELLOW}(default)${NC}"
file_csv="opencsv"

# =============================================================================
# FILE - EXCEL CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File - Excel Configuration${NC}"
select_option "Apache POI ${YELLOW}(default)${NC}" "EasyExcel"
excel_choice=$((1 + $?))
case $excel_choice in
    2)
        file_excel="easyexcel"
        ;;
    *)
        file_excel="poi"
        ;;
esac

# =============================================================================
# FILE - PDF CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File - PDF Configuration${NC}"
select_option "Apache PDFBox ${YELLOW}(default)${NC}"
file_pdf="pdfbox"

# =============================================================================
# KEY-VALUE STORE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Key-Value Store Configuration${NC}"
select_option "Redis ${YELLOW}(default)${NC}"
kv_store="redis"

# =============================================================================
# REDIS CONFIGURATION
# =============================================================================
if [[ "$kv_store" == "redis" ]]; then
    echo -e "${BLUE}${BOLD}Redis Deployment Configuration${NC}"
    select_option "Standalone ${YELLOW}(default)${NC}"
    redis="standalone"
fi

# =============================================================================
# MESSAGING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Messaging Configuration${NC}"
select_option "Apache Kafka ${YELLOW}(default)${NC}" "RabbitMQ"
messaging_choice=$((1 + $?))
case $messaging_choice in
    2)
        messaging="rabbitmq"
        ;;
    *)
        messaging="kafka"
        ;;
esac

# =============================================================================
# MIGRATION CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Migration Configuration${NC}"
select_option "Flyway ${YELLOW}(default)${NC}"
migration="flyway"

# =============================================================================
# EMAIL NOTIFICATION CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Email Notification Configuration${NC}"
select_option "Spring ${YELLOW}(default)${NC}" "SendGrid"
email_choice=$((1 + $?))
case $email_choice in
    2)
        notification_email="sendgrid"
        ;;
    *)
        notification_email="spring"
        ;;
esac

# =============================================================================
# OBSERVABILITY LOGGING STACK CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Logging Stack Configuration${NC}"
select_option "Loki ${YELLOW}(default)${NC}" "None"
logging_choice=$((1 + $?))
case $logging_choice in
    2)
        logging_stack="none"
        ;;
    *)
        logging_stack="loki"
        ;;
esac

# =============================================================================
# OBSERVABILITY LOGGING FRAMEWORK CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Logging Framework Configuration${NC}"
select_option "Logback ${YELLOW}(default)${NC}"
logging_framework="logback"

# =============================================================================
# OBSERVABILITY METRICS CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Metrics Configuration${NC}"
select_option "Prometheus ${YELLOW}(default)${NC}" "None"
metrics_choice=$((1 + $?))
case $metrics_choice in
    2)
        metrics="none"
        ;;
    *)
        metrics="prometheus"
        ;;
esac

# =============================================================================
# OBSERVABILITY TRACING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Tracing Configuration${NC}"
select_option "Zipkin ${YELLOW}(default)${NC}" "None"
tracing_choice=$((1 + $?))
case $tracing_choice in
    2)
        tracing="none"
        ;;
    *)
        tracing="zipkin"
        ;;
esac

# =============================================================================
# SCHEDULER CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Scheduler Configuration${NC}"
select_option "Quartz ${YELLOW}(default)${NC}" "Spring"
scheduler_choice=$((1 + $?))
case $scheduler_choice in
    2)
        scheduler="spring"
        ;;
    *)
        scheduler="quartz"
        ;;
esac

# =============================================================================
# SEARCH CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Search Configuration${NC}"
select_option "Database ${YELLOW}(default)${NC}" "Elasticsearch"
search_choice=$((1 + $?))
case $search_choice in
    2)
        search="elasticsearch"
        ;;
    *)
        search="database"
        ;;
esac

# =============================================================================
# STORAGE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Storage Configuration${NC}"
# On-Premise options: Filesystem and MinIO
select_option "Filesystem ${YELLOW}(default)${NC}" "MinIO"
storage_choice=$((1 + $?))
case $storage_choice in
    2)
        storage="minio"
        ;;
    *)
        storage="filesystem"
        ;;
esac

# =============================================================================
# TEMPLATE ENGINE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Template Engine Configuration${NC}"
select_option "Freemarker ${YELLOW}(default)${NC}" "Thymeleaf"
template_choice=$((1 + $?))
case $template_choice in
    2)
        template="thymeleaf"
        ;;
    *)
        template="freemarker"
        ;;
esac

# =============================================================================
# REVERSE PROXY CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Reverse Proxy Configuration${NC}"
if [[ "$platform" == onprem-k8s* ]]; then
    select_option "Nginx ${YELLOW}(default)${NC}"
    reverse_proxy="nginx"
else
    select_option "Nginx ${YELLOW}(default)${NC}" "None"
    reverse_proxy_choice=$((1 + $?))
    case $reverse_proxy_choice in
        2)
            reverse_proxy="none"
            ;;
        *)
            reverse_proxy="nginx"
            ;;
    esac
fi

echo -e "${PURPLE}${BOLD}Generating ${APP_STACK_FILE} file...${NC}"
sleep 2

# Create the configuration file
cat > "$APP_STACK_FILE_PATH" << EOF
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
observability.logging.stack=$logging_stack
observability.logging.framework=$logging_framework
observability.metrics=$metrics
observability.tracing=$tracing
persistence=jpa
reverse-proxy=$reverse_proxy
scheduler=$scheduler
search=$search
storage=$storage
template=$template
EOF

# Check if file creation was successful
if [ $? -ne 0 ] || [ ! -f "$APP_STACK_FILE_PATH" ]; then
    echo -e "${RED}${BOLD}Error: Failed to create configuration file at ${APP_STACK_FILE_PATH}${NC}"
    echo -e "${RED}Please check if the directory exists and you have write permissions.${NC}"
    exit 1
fi
