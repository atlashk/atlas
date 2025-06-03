# Atlas

## Project Overview

**Atlas** is a microservices-based platform.

---

## Architecture

- **Microservices**: Modular architecture for scalability and flexibility.
- **Hexagonal Architecture**: Separation of concerns to enhance maintainability.
- **Cross-Cutting Concerns**: Centralized handling of concerns such as logging, security, and monitoring.

---

## Technical Stack

---

## Quick Start

This method uses the default infrastructure stack, which is `local-compose-simple`.

### Requirements

- Installed OpenJDK 17
- Installed Docker

### Build the Project

#### Build JAR files

```bash
chmod +x ./script/build-jar.sh
./script/build-jar.sh
```

#### Build Docker images

```bash
chmod +x ./script/build-docker-images.sh
./script/build-docker-images.sh
```

### Start infrastructure services

```bash
chmod +x ./deployment/local/compose/start.sh
./deployment/local/compose/start.sh infra
```

Then wait a few minutes until they start successfully.

### (Optional) Start observability services

```bash
./deployment/local/compose/start.sh observability
```

### Start backend services

```bash
./deployment/local/compose/start.sh backend
```

Then wait a few minutes until the start is complete.

### Start frontend

```bash
cd frontend
npm install
npm run dev
```

The web application will then be accessible at http://localhost:9000.

You can use the following two pre-created accounts to log in.
- Front site: user / Aa@123456
- Admin site: admin / Aa@123456

### Stop and clear

```bash
cd deployment/local/compose

# Just stop containers
./stop.sh all

# Stop containers and remove everything
./stop.sh all --remove-all

# Stop only backend services and remove custom images
./stop.sh backend --remove-images

# Stop infrastructure and remove volumes (careful - data loss!)
./stop.sh infra --remove-volumes
```
