# Stock Control — Backend API

Manufacturing stock control REST API built with **Java 17**, **Quarkus 3**, and **PostgreSQL**.

> Technical test — Junior Full Stack Developer position at Projedata.

## Features

- **Products**: Full CRUD for manufactured products
- **Raw Materials**: Stock management with quantity tracking
- **Product-Material Association**: Define which raw materials compose each product and their quantities
- **Production Suggestions**: Greedy algorithm that calculates optimal production based on available stock, prioritizing highest-value products
- **Automated Tests**: Unit and integration tests with JUnit 5 + REST Assured
- **API Documentation**: Interactive Swagger UI

## Tech Stack

| Layer        | Technology                          |
|-------------|-------------------------------------|
| Language    | Java 17                             |
| Framework   | Quarkus 3.32                        |
| ORM         | Hibernate ORM with Panache          |
| Database    | PostgreSQL 13                       |
| Validation  | Hibernate Validator (Bean Validation)|
| Tests       | JUnit 5 + REST Assured              |
| Docs        | SmallRye OpenAPI (Swagger)          |
| Build       | Maven (via wrapper)                 |
| Container   | Docker + Docker Compose             |

## Architecture

```
HTTP Request → Resource (REST Controller)
                   ↓
               Service (Business Logic)
                   ↓
               Repository (Data Access / Panache)
                   ↓
               Entity (JPA / Database)
```

Three-layer architecture following SOLID principles. DTOs separate the API contract from the database model.

## API Endpoints

### Products (`/api/products`)
| Method | Endpoint              | Description         |
|--------|----------------------|---------------------|
| GET    | `/api/products`       | List all products   |
| GET    | `/api/products/{id}`  | Get product by ID   |
| POST   | `/api/products`       | Create product      |
| PUT    | `/api/products/{id}`  | Update product      |
| DELETE | `/api/products/{id}`  | Delete product      |

### Raw Materials (`/api/raw-materials`)
| Method | Endpoint                  | Description            |
|--------|--------------------------|------------------------|
| GET    | `/api/raw-materials`      | List all materials     |
| GET    | `/api/raw-materials/{id}` | Get material by ID     |
| POST   | `/api/raw-materials`      | Create material        |
| PUT    | `/api/raw-materials/{id}` | Update material/stock  |
| DELETE | `/api/raw-materials/{id}` | Delete material        |

### Product Materials (`/api/products/{productId}/materials`)
| Method | Endpoint                                           | Description                |
|--------|---------------------------------------------------|----------------------------|
| GET    | `/api/products/{productId}/materials`              | List materials for product |
| POST   | `/api/products/{productId}/materials`              | Add material to product    |
| PUT    | `/api/products/{productId}/materials/{materialId}` | Update quantity needed     |
| DELETE | `/api/products/{productId}/materials/{materialId}` | Remove material            |

### Production Suggestions (`/api/production-suggestions`)
| Method | Endpoint                        | Description               |
|--------|---------------------------------|---------------------------|
| GET    | `/api/production-suggestions`   | Calculate production plan |

## Production Suggestion Algorithm

The algorithm uses a **Greedy strategy** to determine what to manufacture:

1. Sort all products by value (descending)
2. For each product, calculate the maximum producible quantity based on available stock
3. "Consume" raw materials from a virtual stock copy
4. Move to the next product with the remaining stock

**Time Complexity**: O(P × M) where P = products, M = materials per product

This approach was chosen because the requirement specifies prioritization by value, not global optimization (which would require Integer Linear Programming).

## Quick Start

### Prerequisites
- Java 17+ (recommended: install via [SDKMAN](https://sdkman.io/))
- Docker and Docker Compose

### Option 1: Docker Compose (Recommended)

```bash
# Build the JAR
./mvnw package -DskipTests

# Start all services (PostgreSQL + Backend)
docker compose up --build

# Access:
# API:     http://localhost:8080/api/products
# Swagger: http://localhost:8080/q/swagger-ui
```

### Option 2: Development Mode

```bash
# Start only PostgreSQL
docker compose up db -d

# Run Quarkus in dev mode (hot reload)
./mvnw quarkus:dev

# Access:
# API:     http://localhost:8080/api/products
# Swagger: http://localhost:8080/q/swagger-ui
```

### Stopping

```bash
# Stop all containers and remove volumes
docker compose down -v
```

## Running Tests

```bash
./mvnw test
```

17 tests across 3 test classes:
- **ProductResourceTest** (9 tests) — CRUD operations, validation, 404 handling
- **ProductionSuggestionServiceTest** (5 tests) — Algorithm logic with multiple scenarios
- **ProductionSuggestionResourceTest** (3 tests) — End-to-end HTTP flow

## Project Structure

```
src/main/java/com/stockproject/stock/
├── dto/                          # Data Transfer Objects
│   ├── ProductDTO.java
│   ├── RawMaterialDTO.java
│   ├── ProductMaterialDTO.java
│   └── ProductionSuggestionDTO.java
├── entity/                       # JPA Entities
│   ├── Product.java
│   ├── RawMaterial.java
│   └── ProductMaterial.java
├── repository/                   # Data Access Layer (Panache)
│   ├── ProductRepository.java
│   ├── RawMaterialRepository.java
│   └── ProductMaterialRepository.java
├── service/                      # Business Logic
│   ├── ProductService.java
│   ├── RawMaterialService.java
│   ├── ProductMaterialService.java
│   └── ProductionSuggestionService.java
└── resource/                     # REST Controllers
    ├── ProductResource.java
    ├── RawMaterialResource.java
    ├── ProductMaterialResource.java
    ├── ProductionSuggestionResource.java
    ├── ErrorMapper.java
    └── CorsFilter.java
```

## Related

- **Frontend**: [stock-project-frontend](https://github.com/alisson-porpino/stock-project-frontend) — React + Redux
