# Microservices Work-Sample

This repository contains a minimal, compile-ready Spring Boot microservices Maven project scaffold that mirrors the architecture in the provided diagram.

## Modules
- api-gateway: Spring Cloud Gateway entry point routing to downstream services.
- auth-server: Spring Authorization Server (placeholder config).
- discovery-server: Eureka service registry.
- config-server: Spring Cloud Config Server (points to a sample Git repo URL).
- product-service: Demo REST endpoints for products.
- order-service: Exposes an order creation endpoint; publishes OrderCreatedEvent to Kafka and is ready for Resilience4j.
- inventory-service: Simple synchronous API placeholder (e.g., stock check).
- notification-service: Consumes OrderCreatedEvent from Kafka.
- common-lib: Shared DTOs/events between services.

## Quick start
1. Build all modules:
   - On Windows PowerShell: `mvn -q -DskipTests package`
2. Run foundational services:
   - discovery-server (8761)
   - config-server (8888)
   - Kafka broker locally on `localhost:9092` (if you want to try async flow)
3. Run application services:
   - api-gateway (8080)
   - product-service (8081)
   - order-service (8082)
   - inventory-service (8083)
   - notification-service (8084)
   - auth-server (9000)

## Try it
- GET http://localhost:8080/products to see demo products.
- POST http://localhost:8082/orders with a JSON body matching `OrderCreatedEvent` to publish an event that notification-service listens to.

## Notes
- Observability (Zipkin) and ELK stack are not included to keep the scaffold minimal, but can be added easily via dependencies and docker-compose.
- Security configuration is intentionally minimal. Fill in the auth-server issuer URI, clients, and gateway filters as needed for your environment.
