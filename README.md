# Atlas

## Project Overview

**Atlas** is a comprehensive microservices-based e-commerce platform built with modern technologies and best practices. It demonstrates Domain-Driven Design (DDD), Event-Driven Architecture, and Clean Architecture principles across multiple bounded contexts including User Management, Product Catalog, Order Processing, and Notifications.

---

## Technical Stack

### Backend

- **Java 17** - Core programming language
- **Spring Boot 3.4.0** - Main application framework
- **Spring Framework 6.2.0** - Core Spring framework
- **Spring Cloud 2024.0.0** - Microservices infrastructure
- **MySQL 8.0** - Primary database
- **Redis 7** - Caching and session storage
- **Apache Kafka 7.9.0** - Event streaming and messaging
- **Gradle** - Build automation tool

### Frontend

- **Vue.js 3.5.14** - Progressive web framework
- **TypeScript 5.8.0** - Type-safe JavaScript
- **Vite 6.3.5** - Build tool and dev server
- **Pinia 3.0.1** - State management
- **Vue Router 4.5.1** - Client-side routing
- **Axios 1.9.0** - HTTP client

### Deployment

- **Docker & Docker Compose** - Containerization
- **Kubernetes** - Container orchestration (optional)

### Observabilities

- **Promptail** & **Loki** - Log aggregation
- **Prometheus** - Metrics
- **Zipkin** - Distributed tracing
- **Grafana** - Visualization and monitoring dashboards

### Architecture Patterns

- **Microservices Architecture** - Service decomposition
- **Domain-Driven Design (DDD)** - Business logic organization
- **Event-Driven Architecture** - Asynchronous communication
- **CQRS** - Command Query Responsibility Segregation
- **Outbox Pattern** - Reliable event publishing

---

## 🚀 Quick Start

### Prerequisites

- **Mininum memory** - 8Gb
- **Java 17+** - For building the project
- **Node.js 22+** - For frontend development
- **Docker & Docker Compose** - For running services

### Backend Startup

For this style, we can easily use our simple wrapper scripts from the project root. They will invoke the relevant on-prem Docker compose scripts.

```bash
# Start services
./startup.sh

# Start services (skip builds)
./startup.sh --skip-build
```

### Frontend Startup

To start frontend, we need to make a `.env` file in `frontend` directory:

```
VITE_API_BASE_URL=http://localhost:8080
```

Then start Frontend in development mode:

```bash
cd frontend
npm install
npm run dev
```

The web application will be accessible at **http://localhost:9000**.

Login Credentials:
- **Customer Portal**: `user` / `Aa@123456`
- **Admin Dashboard**: `admin` / `Aa@123456`
