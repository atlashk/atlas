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
APP_STACK_CONFIG="$PROJECT_ROOT/backend/app-stack.cfg"

# Function to select option using arrow keys
select_option() {
    local options=("$@")
    local max_index=${#options[@]}
    local selected=1

    # If only one option, auto-select it
    if [ $max_index -eq 1 ]; then
        echo -e "  ${CYAN}▶ ${GREEN}1${NC}) "
        echo -e "${options[0]}${NC}"
        echo
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
select_option "On-Premise (Docker Compose) ${YELLOW}(default)${NC}" "On-Premise (Kubernetes)" "AWS (ECS)" "AWS (Lambda)"
platform_choice=$((1 + $?))
case $platform_choice in
    2)
        platform="onprem-k8s"
        ;;
    3)
        platform="aws-ecs"
        ;;
    4)
        platform="aws-lambda"
        ;;
    *)
        platform="onprem-compose"
        ;;
esac
echo

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
echo

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
# CACHE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Cache Configuration${NC}"
select_option "Redis ${YELLOW}(default)${NC}" "Simple"
cache_choice=$((1 + $?))
case $cache_choice in
    2)
        cache="simple"
        ;;
    *)
        cache="redis"
        ;;
esac
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
echo

# =============================================================================
# DISCOVERY CLIENT CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Discovery Client Configuration${NC}"
if [[ "$platform" == "aws-"* ]]; then
    discovery_client="none"
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) None ${YELLOW}(default)${NC}"
elif [[ "$platform" == "onprem-k8s" ]]; then
    discovery_client="kubernetes"
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Kubernetes Built-in DNS ${YELLOW}(default)${NC}"
elif [[ "$platform" == "onprem-compose" ]]; then
    discovery_client="eureka"
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Eureka ${YELLOW}(default)${NC}"
else
    select_option "Eureka ${YELLOW}(default)${NC}" "Kubernetes"
    discovery_client_choice=$((1 + $?))
    case $discovery_client_choice in
        2)
            discovery_client="kubernetes"
            ;;
        *)
            discovery_client="eureka"
            ;;
    esac
fi
echo

# =============================================================================
# FILE CSV CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File - CSV Configuration${NC}"
echo -e "  ${CYAN}▶ ${GREEN}1${NC}) OpenCSV ${YELLOW}(default)${NC}"
file_csv="opencsv"
echo

# =============================================================================
# FILE EXCEL CONFIGURATION
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
echo

# =============================================================================
# FILE PDF CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File - PDF Configuration${NC}"
echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Apache PDFBox ${YELLOW}(default)${NC}"
file_csv="pdfbox"
echo

# =============================================================================
# DISTRIBUTED LOCK CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Distributed Lock Configuration${NC}"
echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Redisson ${YELLOW}(default)${NC}"
lock="redisson"
echo

# =============================================================================
# MESSAGING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Messaging Configuration${NC}"
if [[ "$platform" == aws-* ]]; then
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) SNS+SQS ${YELLOW}(default)${NC}"
    messaging="sns"
else
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Apache Kafka ${YELLOW}(default)${NC}"
    messaging="kafka"
fi
echo

# =============================================================================
# EMAIL NOTIFICATION CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Email Notification Configuration${NC}"
if [[ "$platform" == aws-* ]]; then
    select_option "AWS SES ${YELLOW}(default)${NC}" "SendGrid"
    email_choice=$((1 + $?))

    case $email_choice in
        2)
            notification_email="sendgrid"
            ;;
        *)
            notification_email="ses"
            ;;
    esac
else
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
fi
echo

# =============================================================================
# OBSERVABILITY LOGGING STACK CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Logging Stack Configuration${NC}"
echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Loki ${YELLOW}(default)${NC}"
logging_stack="loki"
echo

# =============================================================================
# OBSERVABILITY LOGGING FRAMEWORK CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Logging Framework Configuration${NC}"
echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Logback ${YELLOW}(default)${NC}"
logging_framework="logback"
echo

# =============================================================================
# OBSERVABILITY METRICS CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Metrics Configuration${NC}"
if [[ "$platform" == aws-* ]]; then
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) CloudWatch ${YELLOW}(default)${NC}"
    metrics="cloudwatch"
else
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Prometheus ${YELLOW}(default)${NC}"
    metrics="prometheus"
fi
echo

# =============================================================================
# OBSERVABILITY TRACING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Tracing Configuration${NC}"
echo -e "  ${CYAN}▶ ${GREEN}1${NC}) Zipkin ${YELLOW}(default)${NC}"
tracing="zipkin"
echo

# =============================================================================
# REDIS CONFIGURATION (conditional)
# =============================================================================
echo -e "${BLUE}${BOLD}Redis Deployment Configuration${NC}"
select_option "Standalone ${YELLOW}(default)${NC}" "Cluster"
redis_choice=$((1 + $?))
case $redis_choice in
    2)
        redis="cluster"
        ;;
    *)
        redis="standalone"
        ;;
esac
echo

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
echo

# =============================================================================
# STORAGE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Storage Configuration${NC}"
if [[ "$platform" == aws-* ]]; then
    # For AWS platforms, only show S3
    echo -e "  ${CYAN}▶ ${GREEN}1${NC}) S3 ${YELLOW}(default)${NC}"
    storage="s3"
else
    # For onprem platforms, show Filesystem and MinIO options
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
fi
echo

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
echo

echo -e "${PURPLE}${BOLD}Generating ${APP_STACK_CONFIG} file...${NC}"
sleep 2

# Create the configuration file
cat > "$APP_STACK_CONFIG" << EOF
api-server=$api_server
api-client=$api_client
cache=$cache
config=yaml
datasource=$datasource
discovery-client=$discovery_client
file.csv=$file_csv
file.excel=$file_excel
lock=$lock
messaging=$messaging
notification.email=$notification_email
observability.logging.stack=$logging_stack
observability.logging.framework=$logging_framework
observability.metrics=$metrics
observability.tracing=$tracing
persistence=jpa
platform=$platform
redis=$redis
scheduler=$scheduler
storage=$storage
template=$template
EOF

# Check if file creation was successful
if [ $? -ne 0 ] || [ ! -f "$APP_STACK_CONFIG" ]; then
    echo -e "${RED}${BOLD}Error: Failed to create configuration file at ${APP_STACK_CONFIG}${NC}"
    echo -e "${RED}Please check if the directory exists and you have write permissions.${NC}"
    exit 1
fi

echo -e "${GREEN}${BOLD}Configuration file created successfully!${NC}"
echo
echo -e "${CYAN}${BOLD}Configuration Summary:${NC}"
echo -e "  ${BLUE}Platform:${NC} ${platform}"
echo -e "  ${BLUE}API Client:${NC} ${api_client}"
echo -e "  ${BLUE}API Server:${NC} ${api_server}"
echo -e "  ${BLUE}Cache:${NC} ${cache}"
echo -e "  ${BLUE}CSV:${NC} ${file_csv}"
echo -e "  ${BLUE}Datasource:${NC} ${datasource}"
echo -e "  ${BLUE}Discovery Client:${NC} ${discovery_client}"
echo -e "  ${BLUE}Email:${NC} ${notification_email}"
echo -e "  ${BLUE}Excel:${NC} ${file_excel}"
echo -e "  ${BLUE}Lock:${NC} ${lock}"
echo -e "  ${BLUE}Messaging:${NC} ${messaging}"
echo -e "  ${BLUE}Observability - Logging Stack:${NC} ${logging_stack}"
echo -e "  ${BLUE}Observability - Logging Framework:${NC} ${logging_framework}"
echo -e "  ${BLUE}Observability - Metrics:${NC} ${metrics}"
echo -e "  ${BLUE}Observability - Tracing:${NC} ${tracing}"
echo -e "  ${BLUE}Redis:${NC} ${redis}"
echo -e "  ${BLUE}Scheduler:${NC} ${scheduler}"
echo -e "  ${BLUE}Storage:${NC} ${storage}"
echo -e "  ${BLUE}Template:${NC} ${template}"
echo

sleep 2
echo -e "${GREEN}${BOLD}Setup complete! Your Atlas stack is ready to go!${NC}"
