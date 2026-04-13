# Inventory Service

The **Inventory Service** manages stock levels for the Retail Management microservices demo. It is a reactive service that ensures stock consistency during high-load periods through asynchronous processing and eventual consistency.

## Features
- **Stock Management**: Tracks inventory levels for products.
- **Reactive Architecture**: Uses Spring Boot WebFlux and Project Reactor for non-blocking operations.
- **Event-Driven Resilience**:
  - Consumes `ProductCreated` events from the Catalog Service to initialize inventory for new products.
  - Consumes `OrderCreated` events from the Order Service to reserve stock.
  - Produces `StockUpdated` events to notify the Catalog Service and Order Service of stock changes.
- **Atomic Operations**: Leverages Couchbase for high-performance, atomic stock updates.
- **Observability**: Built-in tracing with Zipkin and metrics for Prometheus.

## Tech Stack
- **Java 25**
- **Spring Boot 3.5.x** (WebFlux)
- **Spring Data Couchbase Reactive**
- **Spring Kafka**
- **Micrometer Tracing + Zipkin**

## Configuration
- **Port**: 8080 (internal)
- **Database**: `couchbase:11210` (Bucket: `inventory-bucket`)
- **Kafka**: `kafka:9092` (Group ID: `inventory`)
- **Zipkin**: `http://zipkin:9411/api/v2/spans`

## Running the Service

### Prerequisites
- Java 25
- Maven
- Couchbase and Kafka running (or use Docker Compose)

### Locally
```bash
./mvnw spring-boot:run
```

### Docker
```bash
docker build -t inventory-service .
docker run -p 8080:8080 inventory-service
```
