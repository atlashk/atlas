#!/bin/bash

# Development Environment Setup Script for Linux
# This script installs Git, Java 17 (OpenJDK), and Docker

set -e  # Exit on any error
set -u  # Exit on undefined variables

# Check if running as root
check_root() {
    if [[ $EUID -eq 0 ]]; then
        echo "WARNING: This script is running as root. Some operations may behave differently."
    fi
}

# Detect Linux distribution
detect_distro() {
    if [[ -f /etc/os-release ]]; then
        . /etc/os-release
        DISTRO=$ID
        VERSION=$VERSION_ID
    elif [[ -f /etc/redhat-release ]]; then
        DISTRO="rhel"
    elif [[ -f /etc/debian_version ]]; then
        DISTRO="debian"
    else
        echo "ERROR: Cannot detect Linux distribution"
        exit 1
    fi

    echo "INFO: Detected distribution: $DISTRO"
}

# Update package manager
update_packages() {
    echo "INFO: Updating package manager..."

    case $DISTRO in
        ubuntu|debian)
            sudo apt-get update
            ;;
        centos|rhel|fedora)
            if command -v dnf &> /dev/null; then
                sudo dnf update -y
            else
                sudo yum update -y
            fi
            ;;
        arch|manjaro)
            sudo pacman -Sy
            ;;
        *)
            echo "WARNING: Unknown distribution. Skipping package update."
            ;;
    esac
}

# Install Git
install_git() {
    echo "INFO: Installing Git..."

    if command -v git &> /dev/null; then
        echo "SUCCESS: Git is already installed: $(git --version)"
        return 0
    fi

    case $DISTRO in
        ubuntu|debian)
            sudo apt-get install -y git
            ;;
        centos|rhel|fedora)
            if command -v dnf &> /dev/null; then
                sudo dnf install -y git
            else
                sudo yum install -y git
            fi
            ;;
        arch|manjaro)
            sudo pacman -S --noconfirm git
            ;;
        *)
            echo "ERROR: Unsupported distribution for Git installation: $DISTRO"
            return 1
            ;;
    esac

    if command -v git &> /dev/null; then
        echo "SUCCESS: Git installed successfully: $(git --version)"
    else
        echo "ERROR: Git installation failed"
        return 1
    fi
}

# Install Java 17 (OpenJDK)
install_java17() {
    echo "INFO: Installing Java 17 (OpenJDK)..."

    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        if [[ $JAVA_VERSION == 17.* ]]; then
            echo "SUCCESS: Java 17 is already installed: $JAVA_VERSION"
            return 0
        else
            echo "WARNING: Different Java version detected: $JAVA_VERSION. Installing Java 17..."
        fi
    fi

    case $DISTRO in
        ubuntu|debian)
            sudo apt-get install -y openjdk-17-jdk
            ;;
        centos|rhel|fedora)
            if command -v dnf &> /dev/null; then
                sudo dnf install -y java-17-openjdk-devel
            else
                sudo yum install -y java-17-openjdk-devel
            fi
            ;;
        arch|manjaro)
            sudo pacman -S --noconfirm jdk17-openjdk
            ;;
        *)
            echo "ERROR: Unsupported distribution for Java installation: $DISTRO"
            return 1
            ;;
    esac

    # Set JAVA_HOME
    JAVA_HOME_PATH=$(dirname $(dirname $(readlink -f $(which java))))
    echo "export JAVA_HOME=$JAVA_HOME_PATH" >> ~/.bashrc
    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc

    if command -v java &> /dev/null; then
        echo "SUCCESS: Java 17 installed successfully: $(java -version 2>&1 | head -n 1)"
        echo "INFO: JAVA_HOME set to: $JAVA_HOME_PATH"
    else
        echo "ERROR: Java 17 installation failed"
        return 1
    fi
}

# Install Docker
install_docker() {
    echo "INFO: Installing Docker..."

    if command -v docker &> /dev/null; then
        echo "SUCCESS: Docker is already installed: $(docker --version)"
        return 0
    fi

    case $DISTRO in
        ubuntu|debian)
            # Install prerequisites
            sudo apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release

            # Add Docker's official GPG key
            curl -fsSL https://download.docker.com/linux/$DISTRO/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

            # Add Docker repository
            echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/$DISTRO $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

            # Install Docker
            sudo apt-get update
            sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            ;;
        centos|rhel|fedora)
            if command -v dnf &> /dev/null; then
                sudo dnf install -y dnf-plugins-core
                sudo dnf config-manager --add-repo https://download.docker.com/linux/$DISTRO/docker-ce.repo
                sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            else
                sudo yum install -y yum-utils
                sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
                sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
            fi
            ;;
        arch|manjaro)
            sudo pacman -S --noconfirm docker docker-compose
            ;;
        *)
            echo "ERROR: Unsupported distribution for Docker installation: $DISTRO"
            return 1
            ;;
    esac

    # Start and enable Docker service
    sudo systemctl start docker
    sudo systemctl enable docker

    # Add current user to docker group
    sudo groupadd docker
    sudo usermod -aG docker $USER
    newgrp docker

    if command -v docker &> /dev/null; then
        echo "SUCCESS: Docker installed successfully: $(docker --version)"
        echo "WARNING: Please log out and log back in for Docker group changes to take effect"
    else
        echo "ERROR: Docker installation failed"
        return 1
    fi
}

# Verify installations
verify_installations() {
    echo "INFO: Verifying installations..."

    local all_good=true

    # Check Git
    if command -v git &> /dev/null; then
        echo "SUCCESS: ✓ Git: $(git --version)"
    else
        echo "ERROR: ✗ Git: Not found"
        all_good=false
    fi

    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1)
        echo "SUCCESS: ✓ Java: $JAVA_VERSION"
    else
        echo "ERROR: ✗ Java: Not found"
        all_good=false
    fi

    # Check Docker
    if command -v docker &> /dev/null; then
        echo "SUCCESS: ✓ Docker: $(docker --version)"
    else
        echo "ERROR: ✗ Docker: Not found"
        all_good=false
    fi

    if $all_good; then
        echo "SUCCESS: All tools installed successfully!"
    else
        echo "ERROR: Some installations failed. Please check the errors above."
        return 1
    fi
}

# Main function
main() {
    echo "INFO: Starting development environment setup..."
    echo

    check_root
    detect_distro
    update_packages

    echo
    install_git
    echo
    install_java17
    echo
    install_docker
    echo

    # Reload shell environment
    echo "INFO: Reloading shell environment..."
    if [[ -f ~/.bashrc ]]; then
        if source ~/.bashrc 2>/dev/null; then
            echo "SUCCESS: Shell environment reloaded"
        else
            echo "WARNING: Failed to reload ~/.bashrc, but continuing with verification"
        fi
    else
        echo "WARNING: ~/.bashrc not found, skipping shell reload"
        # Try alternative shell configuration files
        if [[ -f ~/.bash_profile ]]; then
            if source ~/.bash_profile 2>/dev/null; then
                echo "SUCCESS: Shell environment reloaded from ~/.bash_profile"
            else
                echo "WARNING: Failed to reload ~/.bash_profile"
            fi
        elif [[ -f ~/.profile ]]; then
            if source ~/.profile 2>/dev/null; then
                echo "SUCCESS: Shell environment reloaded from ~/.profile"
            else
                echo "WARNING: Failed to reload ~/.profile"
            fi
        fi
    fi
    echo

    verify_installations

    echo "SUCCESS: Development environment setup completed!"
}

# Run main function
main "$@"
