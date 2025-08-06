#!/bin/bash

# Check if running on Windows (Git Bash, Cygwin, WSL)
IS_WINDOWS=false
if [[ "$(uname -s)" == *MINGW* ]] || [[ "$(uname -s)" == *CYGWIN* ]] || [[ "$(uname -s)" == *MSYS* ]]; then
    IS_WINDOWS=true
fi

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

# Function to select option using arrow keys or numeric input
select_option() {
    local options=("$@")
    local max_index=${#options[@]}
    local selected=1
    
    # Unix/Linux environment - use arrow keys
    local key

    # Try to hide cursor, but don't fail if tput is not available
    tput civis 2>/dev/null || true
    
    # Display options
    for ((i=0; i<${#options[@]}; i++)); do
        if [ $((i+1)) -eq $selected ]; then
            echo -e "  ${CYAN}>${NC} ${GREEN}$((i+1))${NC}) ${options[$i]}"
        else
            echo -e "    ${GREEN}$((i+1))${NC}) ${options[$i]}"
        fi
    done
    
    # Move cursor up to the first option
    for ((i=0; i<${#options[@]}; i++)); do
        tput cuu1 2>/dev/null || echo -en "\033[1A"
    done
    
    # Handle key presses
    while true; do
        # Read a single key press
        read -s -n 1 key
        
        # Handle arrow keys (they send escape sequences)
        if [[ $key = "\e" ]]; then
            read -s -n 2 key
            
            # Handle up/down arrow keys
            if [[ $key = "[A" ]]; then  # Up arrow
                if [ $selected -gt 1 ]; then
                    # Clear current selection
                    tput cuf 2 2>/dev/null || echo -en "\033[2C"
                    echo -n "  "
                    tput cub 4 2>/dev/null || echo -en "\033[4D"
                    
                    # Move up and update selection
                    selected=$((selected-1))
                    tput cuu1 2>/dev/null || echo -en "\033[1A"
                    echo -n "${CYAN}>${NC}"
                    tput cub 2 2>/dev/null || echo -en "\033[2D"
                fi
            elif [[ $key = "[B" ]]; then  # Down arrow
                if [ $selected -lt $max_index ]; then
                    # Clear current selection
                    tput cuf 2 2>/dev/null || echo -en "\033[2C"
                    echo -n "  "
                    tput cub 4 2>/dev/null || echo -en "\033[4D"
                    
                    # Move down and update selection
                    selected=$((selected+1))
                    tput cud1 2>/dev/null || echo -en "\033[1B"
                    echo -n "${CYAN}>${NC}"
                    tput cub 2 2>/dev/null || echo -en "\033[2D"
                fi
            fi
        elif [[ $key = "" ]]; then  # Enter key
            break
        # Handle numeric input
        elif [[ $key =~ ^[0-9]$ ]]; then
            # Read more digits if available
            local num_input=$key
            read -t 1 -s -n 9 more_digits
            num_input="$num_input$more_digits"
            
            # Validate and use numeric input
            if [[ $num_input =~ ^[0-9]+$ ]] && [ $num_input -ge 1 ] && [ $num_input -le $max_index ]; then
                selected=$num_input
                break
            fi
        fi
    done
    
    # Show cursor again
    tput cnorm 2>/dev/null || true
    
    # Clear the options display
    for ((i=0; i<${#options[@]}; i++)); do
        tput cub 100 2>/dev/null || echo -en "\033[100D"  # Move cursor to beginning of line
        tput el 2>/dev/null || echo -en "\033[K"       # Clear to end of line
        if [ $i -lt $((${#options[@]}-1)) ]; then
            tput cud1 2>/dev/null || echo -en "\033[1B"  # Move cursor down
        fi
    done
    
    # Move cursor back to beginning
    for ((i=1; i<${#options[@]}; i++)); do
        tput cuu1 2>/dev/null || echo -en "\033[1A"
    done
    
    # Display the selected option
    echo -e "${GREEN}Selected: ${options[$((selected-1))]}${NC}"
    
    # Return the selected option
    echo $selected
}

# Header
echo -e "${CYAN}${BOLD}Atlas Stack Configuration${NC}"
echo

# =============================================================================
# PLATFORM CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Platform Configuration${NC}"
platform_choice=$(select_option "On-Premise (Docker Compose) ${YELLOW}(default)${NC}" "On-Premise (Kubernetes)" "AWS (ECS)" "AWS (Lambda)")
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
echo -e "${GREEN}✅ Selected Platform: ${platform}${NC}"
echo

# =============================================================================
# API SERVER CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}API Server Configuration${NC}"
api_server_choice=$(select_option "REST ${YELLOW}(default)${NC}" "gRPC")
case $api_server_choice in
    2)
        api_server="grpc"
        ;;
    *)
        api_server="rest"
        ;;
esac
echo -e "${GREEN}✅ Selected API Server: ${api_server}${NC}"
echo

# =============================================================================
# API CLIENT CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}API Client Configuration${NC}"
if [ "$api_server" = "grpc" ]; then
    api_client_choice=$(select_option "gRPC netdevh ${YELLOW}(default)${NC}")
    api_client="grpc-netdevh"
else
    api_client_choice=$(select_option "Apache HttpClient ${YELLOW}(default)${NC}" "Feign" "RestClient" "RestTemplate")
    
    case $api_client_choice in
        2)
            api_client="rest-feign"
            ;;
        3)
            api_client="rest-restclient"
            ;;
        4)
            api_client="rest-resttemplate"
            ;;
        *)
            api_client="rest-apachehttpclient"
            ;;
    esac
fi
echo -e "${GREEN}✅ Selected API Client: ${api_client}${NC}"
echo

# =============================================================================
# CACHE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Cache Configuration${NC}"
cache_choice=$(select_option "Redis ${YELLOW}(default)${NC}" "Simple")
case $cache_choice in
    2)
        cache="simple"
        ;;
    *)
        cache="redis"
        ;;
esac
echo -e "${GREEN}✅ Selected Cache: ${cache}${NC}"
echo

# =============================================================================
# DATASOURCE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Datasource Configuration${NC}"
datasource_choice=$(select_option "MySQL ${YELLOW}(default)${NC}" "PostgreSQL")
case $datasource_choice in
    2)
        datasource="postgresql"
        ;;
    *)
        datasource="mysql"
        ;;
esac
echo -e "${GREEN}✅ Selected Datasource: ${datasource}${NC}"
echo

# =============================================================================
# DISCOVERY CLIENT CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Discovery Client Configuration${NC}"
if [[ "$platform" == "aws-"* ]]; then
    discovery_client="none"
    echo -e "${YELLOW}⚠️  Discovery Client is not applicable for AWS platforms - set to none${NC}"
elif [[ "$platform" == "onprem-k8s" ]]; then
    discovery_client="kubernetes"
    echo -e "${YELLOW}Using Kubernetes as Discovery Client for Kubernetes platform${NC}"
elif [[ "$platform" == "onprem-compose" ]]; then
    discovery_client="eureka"
    echo -e "${YELLOW}Using Eureka as Discovery Client for Docker Compose platform${NC}"
else
    discovery_client_choice=$(select_option "Eureka ${YELLOW}(default)${NC}" "Kubernetes")
    case $discovery_client_choice in
        2)
            discovery_client="kubernetes"
            ;;
        *)
            discovery_client="eureka"
            ;;
    esac
fi
echo -e "${GREEN}✅ Selected Discovery Client: ${discovery_client}${NC}"
echo

# =============================================================================
# FILE CSV CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File CSV Configuration${NC}"
csv_choice=$(select_option "OpenCSV ${YELLOW}(default)${NC}")
file_csv="opencsv"
echo -e "${GREEN}✅ Selected CSV: ${file_csv}${NC}"
echo

# =============================================================================
# FILE EXCEL CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}File Excel Configuration${NC}"
excel_choice=$(select_option "POI ${YELLOW}(default)${NC}" "EasyExcel")
case $excel_choice in
    2)
        file_excel="easyexcel"
        ;;
    *)
        file_excel="poi"
        ;;
esac
echo -e "${GREEN}✅ Selected Excel: ${file_excel}${NC}"
echo

# =============================================================================
# DISTRIBUTED LOCK CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Distributed Lock Configuration${NC}"
lock_choice=$(select_option "Redisson ${YELLOW}(default)${NC}")
lock="redisson"
echo -e "${GREEN}✅ Selected Lock: ${lock}${NC}"
echo

# =============================================================================
# MESSAGING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Messaging Configuration${NC}"
# Use SNS for AWS platforms, Kafka for onprem platforms
if [[ "$platform" == aws-* ]]; then
    # For AWS platforms, only show SNS
    echo -e "${CYAN}> Using SNS for AWS platform${NC}"
    messaging="sns"
else
    # For onprem platforms, only show Kafka
    echo -e "${CYAN}> Using Kafka for on-premise platform${NC}"
    messaging="kafka"
fi
echo -e "${GREEN}✅ Selected Messaging: ${messaging}${NC}"
echo

# =============================================================================
# EMAIL NOTIFICATION CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Email Notification Configuration${NC}"
if [[ "$platform" == aws-* ]]; then
    # For AWS platforms, show SendGrid and SES options
    echo -e "${CYAN}> Use arrow keys or enter number to select:${NC}"
    email_choice=$(select_option "SendGrid" "SES ${YELLOW}(recommended for AWS)${NC}")

    case $email_choice in
        2)
            notification_email="ses"
            ;;
        *)
            notification_email="sendgrid"
            ;;
    esac
else
    # For onprem platforms, show Spring and SendGrid options
    echo -e "${CYAN}> Use arrow keys or enter number to select:${NC}"
    email_choice=$(select_option "Spring ${YELLOW}(default)${NC}" "SendGrid")

    case $email_choice in
        2)
            notification_email="sendgrid"
            ;;
        *)
            notification_email="spring"
            ;;
    esac
fi
echo -e "${GREEN}✅ Selected Email: ${notification_email}${NC}"
echo

# =============================================================================
# OBSERVABILITY LOGGING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Logging Configuration${NC}"
echo -e "${CYAN}> Use arrow keys or enter number to select:${NC}"
logging_choice=$(select_option "Logback ${YELLOW}(default)${NC}")
logging="logback"
echo -e "${GREEN}✅ Selected Logging: ${logging}${NC}"
echo

# =============================================================================
# OBSERVABILITY METRICS CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Metrics Configuration${NC}"
if [[ "$platform" == aws-* ]]; then
    # For AWS platforms, only show CloudWatch
    echo -e "${CYAN}> Using CloudWatch for AWS platform${NC}"
    metrics="cloudwatch"
else
    # For onprem platforms, only show Prometheus
    echo -e "${CYAN}> Using Prometheus for on-premise platform${NC}"
    metrics="prometheus"
fi
echo -e "${GREEN}✅ Selected Metrics: ${metrics}${NC}"
echo

# =============================================================================
# OBSERVABILITY TRACING CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Observability Tracing Configuration${NC}"
echo -e "${CYAN}> Use arrow keys or enter number to select:${NC}"
tracing_choice=$(select_option "Zipkin ${YELLOW}(default)${NC}")

tracing="zipkin"
echo -e "${GREEN}✅ Selected Tracing: ${tracing}${NC}"
echo

# =============================================================================
# REDIS CONFIGURATION (conditional)
# =============================================================================
echo -e "${BLUE}${BOLD}Redis Configuration${NC}"
echo -e "${CYAN}> Use arrow keys or enter number to select:${NC}"
redis_choice=$(select_option "Standalone ${YELLOW}(default)${NC}" "Cluster")
case $redis_choice in
    2)
        redis="cluster"
        ;;
    *)
        redis="standalone"
        ;;
esac
echo -e "${GREEN}✅ Selected Redis: ${redis}${NC}"
echo

# =============================================================================
# SCHEDULER CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Scheduler Configuration${NC}"
echo -e "${CYAN}> Use arrow keys or enter number to select:${NC}"
scheduler_choice=$(select_option "Quartz ${YELLOW}(default)${NC}" "Spring")
case $scheduler_choice in
    2)
        scheduler="spring"
        ;;
    *)
        scheduler="quartz"
        ;;
esac
echo -e "${GREEN}✅ Selected Scheduler: ${scheduler}${NC}"
echo

# =============================================================================
# STORAGE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Storage Configuration${NC}"
if [[ "$platform" == aws-* ]]; then
    # For AWS platforms, only show S3
    echo -e "${CYAN}> Using S3 for AWS platform${NC}"
    storage="s3"
else
    # For onprem platforms, show Filesystem and MinIO options
    echo -e "${CYAN}> Use arrow keys or enter number to select:${NC}"
    storage_choice=$(select_option "Filesystem ${YELLOW}(default)${NC}" "MinIO")
    case $storage_choice in
        2)
            storage="minio"
            ;;
        *)
            storage="filesystem"
            ;;
    esac
fi
echo -e "${GREEN}✅ Selected Storage: ${storage}${NC}"
echo

# =============================================================================
# TEMPLATE ENGINE CONFIGURATION
# =============================================================================
echo -e "${BLUE}${BOLD}Template Engine Configuration${NC}"
echo -e "${CYAN}> Use arrow keys or enter number to select:${NC}"
template_choice=$(select_option "Freemarker ${YELLOW}(default)${NC}" "Thymeleaf")
case $template_choice in
    2)
        template="thymeleaf"
        ;;
    *)
        template="freemarker"
        ;;
esac
echo -e "${GREEN}✅ Selected Template: ${template}${NC}"
echo

# Generate app-stack.cfg file
config_file="backend/app-stack.cfg"

echo -e "${PURPLE}${BOLD}Generating Configuration...${NC}"
echo -e "${YELLOW}Creating file: ${config_file}${NC}"
echo

cat > "$config_file" << EOF
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
observability.logging=$logging
observability.metrics=$metrics
observability.tracing=$tracing
persistence=jpa
platform=$platform
redis=$redis
scheduler=$scheduler
storage=$storage
template=$template
EOF

echo -e "${GREEN}${BOLD}Configuration file created successfully!${NC}"
echo
echo -e "${CYAN}${BOLD}Configuration Summary:${NC}"
echo -e "  ${BLUE}Platform:${NC} ${platform}"
echo -e "  ${BLUE}API Server:${NC} ${api_server}"
echo -e "  ${BLUE}API Client:${NC} ${api_client}"
echo -e "  ${BLUE}Datasource:${NC} ${datasource}"
echo -e "  ${BLUE}Cache:${NC} ${cache}"
echo -e "  ${BLUE}Redis:${NC} ${redis}"
echo -e "  ${BLUE}Messaging:${NC} ${messaging}"
echo -e "  ${BLUE}Email:${NC} ${notification_email}"
echo -e "  ${BLUE}Logging:${NC} ${logging}"
echo -e "  ${BLUE}Metrics:${NC} ${metrics}"
echo -e "  ${BLUE}Tracing:${NC} ${tracing}"
echo -e "  ${BLUE}CSV:${NC} ${file_csv}"
echo -e "  ${BLUE}Excel:${NC} ${file_excel}"
echo -e "  ${BLUE}Lock:${NC} ${lock}"
echo -e "  ${BLUE}Scheduler:${NC} ${scheduler}"
echo -e "  ${BLUE}Storage:${NC} ${storage}"
echo -e "  ${BLUE}Template:${NC} ${template}"
echo -e "  ${BLUE}Discovery:${NC} ${discovery_client}"
echo -e "  ${BLUE}Config File:${NC} ${config_file}"
echo
echo -e "${GREEN}${BOLD}Setup complete! Your Atlas stack is ready to go!${NC}"
