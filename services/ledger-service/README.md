# Ledger Service

Ledger Service is the foundation for DioBank's immutable financial ledger. This service is configured for Java 21, Spring Boot 3.x, Gradle, PostgreSQL, Flyway, gRPC, Docker, and Actuator health checks.

This foundation intentionally contains no ledger posting logic, no business RPC implementation, no Kafka publishing, and no balance calculation.

## Runtime

- HTTP port: `8080`
- gRPC port: `9090`
- Health endpoint: `/actuator/health`
- Database: PostgreSQL via Docker Compose
- Migrations: Flyway configured at `classpath:db/migration`

## Run With Docker Compose

From the repository root:

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

Stop the stack:

```bash
docker compose -f infra/docker/docker-compose.yml down
```

Remove the PostgreSQL volume:

```bash
docker compose -f infra/docker/docker-compose.yml down -v
```

## Build Locally

The preferred development path is Docker-first. If Gradle and Java 21 are available locally, the service can also be built from this directory:

```bash
gradle clean build
```

## Configuration

Environment variables used by Docker Compose:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `LEDGER_GRPC_PORT`
- `SPRING_PROFILES_ACTIVE`

## Scope Boundary

Ledger Service does not own customer data, account metadata, account balance snapshots, balance projections, notifications, settlement, or clearing. Those responsibilities stay outside this service.

