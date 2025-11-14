# Telco Product Catalog – Backend

This repository contains the backend API for the Telco Product Catalog task.  
It is implemented in Spring Boot and exposes a REST API for querying products imported from CSV and JSON files.

## Features

- Imports product data from:
  - `incompatible_feeds_products.csv`
  - `incompatible_feeds_products.json`
- Stores imported products in an H2 in-memory database
- Resolves duplicates based on SKU
- Provides validation flags
- Provides a REST API with:
  - filtering
  - sorting
  - valid-only parameter
- Fully dockerized
- Can be integrated with the Angular frontend using Docker Compose

## Requirements

- Docker
- Docker Compose
- (Optional) Java 21+ and Maven if running without Docker

## Running the backend with Docker Compose (recommended)

This repository contains a `docker-compose.yml` file that runs the backend and the frontend together.

Start both frontend and backend:

```
docker compose up --build
```

After startup:

Backend API:  
http://localhost:8080/products

Frontend:  
http://localhost

## Running the backend manually (without Docker)

```
./mvnw spring-boot:run
```

Backend will start at:

```
http://localhost:8080
```

## REST API

### GET /products

Query parameters:

- `filter` (optional): substring filter on name or manufacturer
- `sort` (optional): format `field,direction`
- `validOnly` (optional, default: false): if true, returns only valid products

Example:

```
GET http://localhost:8080/products?filter=cable&sort=name,asc&validOnly=true
```

## Frontend integration

The Angular UI is located in a separate repository.  
When running through Docker Compose, the frontend communicates with this backend service via the Docker network hostname:

```
http://backend:8080
```

## Project structure

- `src/main/java/.../service/ProductImportService.java` – CSV/JSON import logic
- `src/main/java/.../controller/ProductController.java` – REST API
- `src/main/resources` – static resources and sample data
