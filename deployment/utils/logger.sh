#!/bin/bash

# Logging utility functions
# Colors for output - Spring Boot compatible colors
RED='\033[0;31m'      # ERROR
YELLOW='\033[1;33m'   # WARN
GREEN='\033[0;32m'    # INFO
CYAN='\033[0;36m'     # DEBUG
MAGENTA='\033[0;35m'  # TRACE
NC='\033[0m' # No Color

# Log levels
LOG_LEVEL_DEBUG=0
LOG_LEVEL_INFO=1
LOG_LEVEL_WARN=2
LOG_LEVEL_ERROR=3

# Default log level (can be overridden by environment variable)
CURRENT_LOG_LEVEL=${LOG_LEVEL:-$LOG_LEVEL_INFO}

# Get timestamp
get_timestamp() {
    date '+%Y-%m-%d %H:%M:%S'
}

# Generic log function
log() {
    local level=$1
    local color=${2:-$NC}  # Default to NC (No Color) if color is not provided
    local message=${3:-}   # Default to empty string if message is not provided
    local timestamp=$(get_timestamp)
    
    if [ "$level" -ge "$CURRENT_LOG_LEVEL" ]; then
        echo -e "${color}[$(printf "%-5s" "$4")] ${timestamp} - ${message}${NC}"
    fi
}

# Specific log functions
log_debug() {
    log $LOG_LEVEL_DEBUG $NC "$1" "DEBUG"  # Changed back to NC (No Color)
}

log_info() {
    log $LOG_LEVEL_INFO $NC "$1" "INFO"    # Changed back to NC (No Color)
}

log_warn() {
    log $LOG_LEVEL_WARN $YELLOW "$1" "WARN"
}

log_error() {
    log $LOG_LEVEL_ERROR $RED "$1" "ERROR"
}

log_success() {
    log $LOG_LEVEL_INFO $GREEN "$1" "SUCCESS"
}

# Progress indicator
log_progress() {
    echo -ne "${MAGENTA}[PROGRESS] $1${NC}\r"
}

# Clear progress line
clear_progress() {
    echo -ne "\033[2K\r"
}

# Log with separator
log_separator() {
    echo -e "${CYAN}================================================================================${NC}"
}

# Log section header
log_section() {
    log_separator
    log_info "$1"
    log_separator
}
