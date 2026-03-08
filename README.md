# Atlas

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.5.4](https://img.shields.io/badge/Spring%20Boot-3.5.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2025.0.0](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Templates-326CE5?logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Kafka-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Flyway](https://img.shields.io/badge/Flyway-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-2C3E50?logoColor=white)](https://resilience4j.readme.io/)
[![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?logo=prometheus&logoColor=white)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-F46800?logo=grafana&logoColor=white)](https://grafana.com/)
[![Zipkin](https://img.shields.io/badge/Zipkin-000000?logoColor=white)](https://zipkin.io/)
[![Next.js 16](https://img.shields.io/badge/Next.js-16-000000?logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![React 19](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-38B2AC?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Axios](https://img.shields.io/badge/Axios-5A29E4?logo=axios&logoColor=white)](https://axios-http.com/)
[![Stripe](https://img.shields.io/badge/Stripe-635BFF?logo=stripe&logoColor=white)](https://stripe.com/)

## Table of Contents

- Project Overview
- Quick Start (Local)
- Architecture
- Services
- App Stack
- Project Structure
- Technology Stack
- Troubleshooting
- Contributing
- License

---

## Project Overview

Atlas is a microservices-based e-commerce platform showcasing **DDD + Hexagonal Architecture** and a configurable infrastructure stack for local and containerized environments.

Each backend service follows a consistent module layout:

- `domain`: entities, errors, exceptions
- `port`: inbound and outbound contracts
- `application`: use cases and orchestration
- `api`: transport layer (REST/gRPC models + controllers/services)
- `infrastructure`: adapters (JPA, messaging, files, external APIs)
- `bootstrap`: Spring Boot runtime wiring

Shared backend building blocks live under `backend/libs/` and are consumed by multiple services.

```mermaid
flowchart LR
    Client["Client / External Systems"]

    subgraph Bootstrap["**bootstrap** module"]

        API["**api** module<br/>REST / gRPC controllers"]
        APP["**application** module<br/>Application use cases"]
        DOMAIN["**domain** module<br/>Core business logic"]
        PORTS["**port** module<br/>inbound & outbound interfaces"]
        INFRA["**infrastructure** module<br/>JPA, messaging, external APIs, etc."]
        LIBS["**libs** modules<br/>Shared building blocks"]

        PORTS --> DOMAIN
        APP --> PORTS
        API --> PORTS
        INFRA --> PORTS

    end

    Client --> API
```

---

## Technology Stack

### Backend

- **Core**: Java 17, Spring Boot 3.5, Spring Cloud 2025, Gradle (multi-module)
- **API & Communication**: Spring Cloud Gateway, REST (Spring MVC), OpenAPI (springdoc), gRPC
- **Service Platform**: Eureka Server (service discovery), Spring Cloud Config
- **Data & Infrastructure**: MySQL, PostgreSQL, Redis, Kafka, RabbitMQ, Elasticsearch, MinIO, Quartz
- **Security & Integration**: Spring Security (JWT), Keycloak, Stripe, Sendgrid
- **Microservices Patterns**: Saga orchestration, Outbox pattern, Resilience4j
- **Observability**: Actuator, Logback, Loki + Promtail, Prometheus, Zipkin, Grafana
- **Productivity**: Lombok, MapStruct
- **Deployment**: Docker Compose, Kubernetes (native manifests or Helm)

### Frontend

- **Framework**: Next.js 16 (React 19), TypeScript
- **UI**: Tailwind CSS, shadcn/ui, Radix UI
- **State & Forms**: Zustand, React Hook Form, Zod

---

## Project Structure

```
.
├── backend/
│   ├── app-stack/
│   │   ├── config/             # app-stack YAML definitions (drives build + deploy)
│   │   ├── generator/          # Node.js template renderer (Handlebars)
│   │   └── templates/          # Compose/Kubernetes templates per app-stack
│   ├── libs/
│   │   ├── framework/          # shared foundation used by all backend services
│   │   └── building-blocks/    # shared building blocks (API server/client, persistence, messaging, observability, saga, outbox, etc.)
│   ├── platform/
│   │   ├── discovery-server/   # Eureka Server
│   │   └── config-server/      # Spring Cloud Config Server
│   ├── scripts/                # helper scripts (setup, local tooling)
│   ├── services/
│   │   ├── api-gateway/        # Spring Cloud Gateway edge service
│   │   ├── identity-service/   # authentication and user management
│   │   ├── catalog-service/    # product catalog
│   │   ├── inventory-service/  # stock management
│   │   ├── order-service/      # checkout + saga orchestration
│   │   └── payment-service/    # payments + webhooks/simulator
│   ├── build.gradle
│   ├── install.sh
│   └── uninstall.sh
├── frontend/
│   ├── admin/                 # Next.js admin dashboard
│   └── storefront/            # Next.js customer-facing storefront
```

---

## Quick Start (Local)

### Prerequisites

- Java 17+
- Node.js (for frontend and deployment generator)
- Docker Desktop with Docker Compose

Notes:
- On Windows, use Git Bash or WSL to run `./install.sh` in `backend/`.

### Backend

```bash
cd backend
./install.sh
```

This generates `backend/dist/` from templates and then runs the generated install script.

To uninstall:

```bash
cd backend
./uninstall.sh
```

To uninstall and remove application Docker images:

```bash
cd backend
./uninstall.sh --remove-images
```

### Frontend

Both frontends default to talking to the gateway at `http://localhost:8080` as default. You can override this by setting `NEXT_PUBLIC_API_BASE_URL` in `.env`.

#### Storefront

```bash
cd frontend/storefront
npm install
npm run dev
```

Open: http://localhost:8000

Login credentials: `demo` / `Atlas@123456`

#### Admin

```bash
cd frontend/admin
npm install
npm run dev
```

Open: http://localhost:8001

Login credentials: `admin` / `Atlas@123456`

---

## Architecture

### System Components

**Microservices**

| Component | Responsibility | Default URL |
| --- | --- | --- |
| API Gateway | Routing, security, aggregated OpenAPI docs | http://localhost:8080 |
| Identity Service | Authentication and user management | http://localhost:8081 |
| Catalog Service | Product catalog and admin operations | http://localhost:8082 |
| Inventory Service | Stock management | http://localhost:8083 |
| Order Service | Checkout and saga orchestration | http://localhost:8084 |
| Payment Service | Payment processing | http://localhost:8085 |

**Platform**

| Component | Responsibility | Default URL |
| --- | --- | --- |
| Eureka Server | Service discovery for microservices (not used in Kubernetes) | http://localhost:8761 |

**Infrastructure**

| Component | Responsibility | Exposed Ports |
| --- | --- | --- |
| MySQL | Database | 3306 |
| PostgreSQL | Database | 5432 |
| Redis | Key-value store | 6379 |
| Elasticsearch | Full-text search engine | 9200 |
| MinIO | S3-compatible object storage | 9000 |
| Kafka | Messaging platform | 9092 |
| RabbitMQ | Messaging platform | 5672 & 15672 (management UI) |
| Keycloak | Identity provider | 8443 |
| Loki | Log aggregation backend | 3100 |
| Promtail | Log shipping agent | 9080 |
| Prometheus | Metrics scraping and storage | 9090 |
| Zipkin | Distributed tracing backend | 9411 |
| Grafana | Observability visualization dashboards | 3000 |

Default credentials: `atlas` / `Atlas@123456`

**Frontend**

| Component | Responsibility | Exposed Ports |
| --- | --- | --- |
| Storefront | Customer-facing web store | 8000 |
| Admin | Product catalog and order management | 8001 |

### App Stack

Atlas provides an **app-stack configuration mechanism** that enables application and infrastructure technologies to be switched by simply changing values in a YAML configuration file.

| App stack | Target | How to run |
| --- | --- | --- |
| `local.compose` | Docker Compose (default) | `./install.sh` |
| `local.debug` | Docker Compose (debug-friendly templates) | `./install.sh --app-stack=local.debug` |
| `local.k8s.native` | Kubernetes manifests | `./install.sh --app-stack=local.k8s.native` |
| `local.k8s.helm` | Helm chart templates | `./install.sh --app-stack=local.k8s.helm` |

How it works:

- Build-time wiring (Gradle): `backend/build.gradle` loads the selected YAML into `ext.appStack`,
  and sub-modules use it to pick concrete implementations (for example: mysql vs postgres, kafka
  vs rabbitmq, redis connector variants, observability on/off).
- Deploy-time generation (Templates): `backend/install.sh` renders the matching templates into
  `backend/dist/` (Compose / K8s), then runs the generated `install.sh` to bring the stack up.

```mermaid
flowchart TB
  CLI["Run installer<br/>cd backend && ./install.sh --app-stack=..."] --> CFG["app-stack YAML<br/>backend/app-stack/config/app-stack.*.yml"]

  subgraph Build["Build (Gradle)"]
    CFG --> GRADLE["Load config into ext.appStack variable"]
    GRADLE --> SELECT["Select implementations based on ext.appStack value"]
    SELECT --> ARTIFACTS["Build artifacts + Docker images"]
  end

  subgraph Deploy["Deploy"]
    CFG --> GEN["Render Handlebars templates"]
    GEN --> DIST["Generate manifests into backend/dist directory"]
    DIST --> TARGET["Deploy using generated manifests"]
  end

  CLI --> GRADLE
  CLI --> GEN
  ARTIFACTS --> TARGET
```

Common flags:
- `--skip-build`: skips backend and Docker image builds.
- `--debug-template`: generates `backend/dist/` only, does not execute install.

Examples:

```bash
cd backend
./install.sh --app-stack=local.compose
./install.sh --app-stack=local.k8s.native --debug-template
./install.sh --skip-build
```

#### App-Stack Infrastructure Options

| Capability | Options |
|---|---|
| `datasource` | `mysql` \| `postgres` |
| `file.csv` | `opencsv` |
| `file.excel` | `poi` \| `easyexcel` |
| `file.pdf` | `pdfbox` |
| `full-text-search` | `elasticsearch` |
| `identity` | `jwt` \| `keycloak` |
| `internal` | `rest` \| `grpc` |
| `kv-store` | `redis` |
| `messaging` | `kafka` \| `rabbitmq` |
| `migration` | `flyway` |
| `notification.email` | `spring` \| `sendgrid` |
| `observability.logging.framework` | `logback` |
| `observability.logging.stack` | `none` \| `loki` |
| `observability.metrics` | `none` \| `prometheus` |
| `observability.tracing` | `none` \| `zipkin` |
| `persistence` | `jpa` |
| `redis` | `standalone` \| `cluster` |
| `scheduler` | `spring` \| `quartz` |
| `service-discovery` | `eureka` \| `kubernetes` |
| `storage` | `minio` \| `filesystem` |
| `template` | `freemarker` \| `thymeleaf` |

### Checkout Flow

```mermaid
sequenceDiagram
  participant FE as Frontend
  participant GW as API Gateway
  participant ORD as Order Service (Saga orchestrator)
  participant MQ as Messaging (Kafka/RabbitMQ)
  participant INV as Inventory Service
  participant PAY as Payment Service
  participant EXT as External Payment Gateway

  FE->>GW: POST /services/order/api/front/checkout
  GW->>ORD: POST /api/front/checkout
  ORD->>MQ: RESERVE_STOCK command
  MQ->>INV: RESERVE_STOCK command
  INV->>MQ: RESERVE_STOCK reply
  ORD->>MQ: INITIALIZE_PAYMENT command
  MQ->>PAY: INITIALIZE_PAYMENT command
  PAY->>MQ: INITIALIZE_PAYMENT reply
  PAY->>EXT: Create/confirm payment (redirect/QR/webhook)
  EXT-->>PAY: Webhook payment result
  PAY->>MQ: PROCESS_PAYMENT reply
  ORD-->>FE: 201 Created (orderId)
```

---

## Contributing

- Issues and PRs are welcome
- Keep modules aligned with DDD/Clean Architecture
- Prefer adding adapters over changing domain logic

## License

- No LICENSE file is included in this repository
- Add a `LICENSE` file if you plan to distribute
