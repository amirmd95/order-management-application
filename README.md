# Order Management Application

This repository contains a Java 17, Spring Boot 4.0.3 microservices platform for order management. The solution includes:

- `product-service` for product catalog management
- `inventory-service` for stock tracking and reservations
- `order-service` for order orchestration across product and inventory services
- PostgreSQL for persistence
- Docker and Docker Compose for local development
- Kubernetes manifests for cluster deployment
- AWS EKS deployment assets for production rollout

## Architecture

```text
Client -> order-service -> product-service
                      -> inventory-service

product-service   -> PostgreSQL
inventory-service -> PostgreSQL
order-service     -> PostgreSQL
```

## Services

| Service | Port | Responsibility |
| --- | --- | --- |
| `product-service` | `8081` | Manage catalog products and pricing |
| `inventory-service` | `8082` | Maintain inventory levels and reservations |
| `order-service` | `8083` | Validate product data, reserve inventory, and persist orders |

## Local Run

### 1. Build the project

```bash
mvn clean verify
```

### 2. Run the Angular frontend

Angular `21.1.x` officially supports Node `20.19+`, `22.12+`, or `24+`. The frontend is configured to proxy API calls to the local microservices.

```bash
cd frontend
npm install
npm start
```

The Angular dev server runs on `http://localhost:4200`.

### 3. Run with Docker Compose

```bash
docker compose up --build
```

This starts all three services and three PostgreSQL databases.

## Example API Flow

### Create a product

```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SKU-1001",
    "name": "Laptop Stand",
    "description": "Adjustable aluminum stand",
    "price": 1499.00,
    "currency": "INR",
    "active": true
  }'
```

### Seed inventory

```bash
curl -X POST http://localhost:8082/api/inventory/stock \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "SKU-1001",
    "quantity": 25
  }'
```

### Place an order

```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "buyer@example.com",
    "currency": "INR",
    "items": [
      {
        "sku": "SKU-1001",
        "quantity": 2
      }
    ]
  }'
```

## Docker and Kubernetes

- `docker-compose.yml` provisions the local environment.
- `k8s/base` contains Kubernetes manifests for the three services.
- `aws/eks` contains EKS-oriented deployment files and rollout steps for AWS.

## AWS Deployment Strategy

The production path assumes:

- Amazon EKS for orchestration
- Amazon ECR for container images
- Amazon RDS for PostgreSQL databases
- AWS Load Balancer Controller for ingress

See `aws/eks/README.md` for the deployment workflow.
