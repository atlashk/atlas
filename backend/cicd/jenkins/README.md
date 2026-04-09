# Jenkins CI/CD Setup Guide

This pipeline builds individual backend services, packages them as Docker images, pushes them to AWS ECR, and deploys to AWS EKS using Helm.

---

## Table of Contents

1. [Infrastructure Requirements](#1-infrastructure-requirements)
2. [Install Jenkins on EC2 Ubuntu](#2-install-jenkins-on-ec2-ubuntu)
3. [Install Required Tools](#3-install-required-tools)
4. [Configure Jenkins](#4-configure-jenkins)
5. [Create Jenkins Credentials](#5-create-jenkins-credentials)
6. [Install Jenkins Plugins](#6-install-jenkins-plugins)
7. [Create the Pipeline Job](#7-create-the-pipeline-job)
8. [Running the Pipeline](#8-running-the-pipeline)
9. [Environment Variables to Adjust](#9-environment-variables-to-adjust)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Infrastructure Requirements

| Component | Minimum Requirement |
|---|---|
| EC2 instance | t3.medium (2 vCPU, 4 GB RAM) or larger |
| OS | Ubuntu 22.04 LTS |
| AWS ECR | Repository prefix `atlas/` |
| AWS EKS | Clusters named `atlas-<env>` (dev / stg / prd) |
| IAM Role / User | ECR and EKS permissions (see below) |

### Required IAM Permissions

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:PutImage",
        "ecr:CreateRepository",
        "ecr:DescribeRepositories"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "eks:DescribeCluster",
        "eks:ListClusters"
      ],
      "Resource": "*"
    }
  ]
}
```

---

## 2. Install Jenkins on EC2 Ubuntu

Refer: https://www.jenkins.io/doc/book/installing/linux/

```bash
# Update the system
sudo apt update && sudo apt upgrade -y

# Install Java 17 JDK (JDK is required — JRE alone is not enough for Gradle Toolchain)
sudo apt install -y fontconfig openjdk-17-jdk

java -version   # expected: openjdk 17.x.x
javac -version  # expected: javac 17.x.x

# Add the Jenkins apt repository
sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key
echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt update
sudo apt install jenkins

# Enable and start Jenkins
sudo systemctl enable jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins

# Retrieve the initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

Open Jenkins at `http://<EC2-PUBLIC-IP>:8080`, enter the password above, and complete the setup wizard.

> **Security note:** Restrict port 8080 in the EC2 Security Group to trusted IPs only, or place Jenkins behind a Nginx/ALB reverse proxy with HTTPS.

---

## 3. Install Required Tools

Run the following commands as `root` or with `sudo`. Verify each tool is accessible under the `jenkins` user afterward.

### 3.1 Docker

```bash
sudo apt install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) \
    signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# Allow the jenkins user to run Docker without sudo
sudo usermod -aG docker jenkins

# Restart Jenkins to apply the group membership
sudo systemctl restart jenkins
```

### 3.2 AWS CLI v2

```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo apt install -y unzip
unzip awscliv2.zip
sudo ./aws/install
aws --version   # expected: aws-cli/2.x.x
rm -rf awscliv2.zip aws/
```

### 3.3 kubectl

```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
kubectl version --client
```

### 3.4 Helm 3

```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
helm version
```

### 3.5 Gradle

The pipeline uses the Gradle Wrapper (`./gradlew`), so a system-wide Gradle installation is **not required**. However, **JDK 17** (not just JRE) must be available for the `jenkins` user, as Gradle Toolchain requires `javac`:

```bash
# Verify both java and javac are available
sudo -u jenkins java -version
sudo -u jenkins javac -version
```

If `javac` is missing, install the full JDK:

```bash
sudo apt install -y openjdk-17-jdk
```

If `JAVA_HOME` is not set, add it to `/etc/environment` (applies system-wide, including the `jenkins` user):

```bash
# Find the exact JDK path
sudo update-java-alternatives -l
# e.g. output: java-1.17.0-openjdk-amd64  1711  /usr/lib/jvm/java-1.17.0-openjdk-amd64

# Set JAVA_HOME in /etc/environment
echo 'JAVA_HOME=/usr/lib/jvm/java-1.17.0-openjdk-amd64' | sudo tee -a /etc/environment

# Reload environment variables
source /etc/environment

# Verify
sudo -u jenkins printenv JAVA_HOME
# expected: /usr/lib/jvm/java-1.17.0-openjdk-amd64

# Restart Jenkins to pick up the new env var
sudo systemctl restart jenkins
```

> **Note:** Do **not** use `export` in `/etc/environment` — it uses simple `KEY=VALUE` format without the `export` keyword.

---

## 4. Install Jenkins Plugins

Go to **Manage Jenkins > System Configuration > Plugins > Available plugins** and install:

| Plugin | Purpose |
|---|---|
| **Pipeline** | Run Declarative Pipelines |
| **Git** | Git SCM |
| **Git Parameter** | Show branch/tag dropdown in Build with Parameters |
| **Pipeline: AWS Steps** | Provides the `withAWS()` step |
| **AWS Credentials** | Store Amazon IAM credentials in Jenkins Credentials API |
| **JUnit** | Publish test results |
| **Timestamper** | Add timestamps to build logs |

Restart Jenkins after installation:

```bash
sudo systemctl restart jenkins
```

---

## 5. Configure Jenkins

### 5.1 JDK Tool Configuration

1. Go to **Manage Jenkins > System Configuration > Tools > JDK installations**
2. Click **Add JDK** and uncheck "Install automatically"
3. Name: `JDK-17`
4. `JAVA_HOME`: `/usr/lib/jvm/java-17-openjdk-amd64`
5. Click **Save**

### 5.2 Git

```bash
sudo apt install -y git
git --version
```

Jenkins usually auto-detects Git. Verify at **Manage Jenkins > System Configuration > Tools > Git installations**.

---

## 6. Create Jenkins Credentials

Go to **Manage Jenkins > Security > Credentials > System > Global credentials > Add Credentials**.

### 6.1 AWS Account ID

| Field | Value |
|---|---|
| Kind | Secret text |
| Secret | `<AWS_ACCOUNT_ID>` (12-digit number) |
| ID | `AWS_ACCOUNT_ID` |
| Description | AWS Account ID |

### 6.2 AWS Access Keys

| Field | Value |
|---|---|
| Kind | AWS Credentials |
| ID | `aws-credentials` |
| Access Key ID | `<IAM_ACCESS_KEY_ID>` |
| Secret Access Key | `<IAM_SECRET_ACCESS_KEY>` |
| Description | AWS credentials for ECR & EKS |

> The ID `aws-credentials` must match `AWS_CREDENTIALS_ID` in the Jenkinsfile.

---

## 7. Create the Pipeline Job

1. Jenkins Dashboard > **New Item**
2. Enter name: `atlas-backend`
3. Select **Pipeline** > **OK**
4. Under the **General** tab:
   - Check on **Github project**
   - Enter the project URL
4. Under the **Pipeline** tab:
   - **Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: your repository URL
   - **Branch**: `*/main` (or the appropriate branch)
   - **Script Path**: `backend/deployment/cicd/jenkins/Jenkinsfile`
5. Click **Save**

> On the very first save, click **Build Now** once so Jenkins loads the `parameters` block from the Jenkinsfile. This initial build may fail that is expected. From the second run onward, the parameter form will appear.

---

## 8. Running the Pipeline

1. Open the `atlas-backend` job
2. Click **Build with Parameters**
3. Fill in the parameters:

| Parameter | Description | Example |
|---|---|---|
| `SERVICE_NAME` | Service to build and deploy | `catalog-service` |
| `IMAGE_TAG` | Docker image tag | Leave blank ? uses git short SHA |
| `ENVIRONMENT` | Target environment | `dev` / `stg` / `prd` |
| `SKIP_TESTS` | Skip unit/integration tests | `false` |

4. Click **Build**

### Pipeline Stages

```
Preparation          ? Resolve GRADLE_MODULE, SERVICE_DIR, image tag
Checkout             ? Clone source code from SCM
Gradle Build         ? ./gradlew <module>:bootJar
Docker Build & Push  ? Build image and push to ECR
Configure kubectl    ? aws eks update-kubeconfig
Deploy via Helm      ? helm upgrade --install
Verify Rollout       ? kubectl rollout status
```

---

## 9. Environment Variables to Adjust

Open the `Jenkinsfile` and update the following values to match your infrastructure:

```groovy
AWS_REGION = 'us-east-1'   // Region where ECR and EKS reside
```

EKS cluster names and Kubernetes namespaces are derived from the `ENVIRONMENT` parameter:

```
atlas-dev   (ENVIRONMENT = dev)
atlas-stg   (ENVIRONMENT = stg)
atlas-prd   (ENVIRONMENT = prd)
```

If your cluster names follow a different convention, update:

```groovy
EKS_CLUSTER_NAME = "atlas-${params.ENVIRONMENT}"
K8S_NAMESPACE    = "atlas-${params.ENVIRONMENT}"
```
