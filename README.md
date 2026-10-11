# Library Management API

A containerized Spring Boot REST API for library operations, with JWT authentication, role-based access control, Redis caching, and OpenAPI documentation.

## Features

- JWT authentication with refresh token rotation
- Role-based access control with Spring Security
- Redis caching on read-heavy endpoints, switchable with `APP_CACHE_ENABLED`
- Swagger / OpenAPI documentation
- Email sending, tested locally with MailHog (fake SMTP)
- AOP aspects and SLF4J logging
- Actuator health endpoint at `/actuator/health`
- Automated tests: `@DataJpaTest`, `@WebMvcTest`, and `@SpringBootTest` with `MockMvc`
- Docker Compose stack: app, MySQL, Redis, MailHog, plus optional phpMyAdmin and Redis Commander

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language / Framework | Java, Spring Boot |
| Security | Spring Security, JWT |
| Database | MySQL 8 (`librarymanagement` schema), H2 for slice tests |
| Caching | Redis 7 |
| Docs | Swagger / OpenAPI |
| Build | Maven (wrapper included) |
| Containers | Docker, Docker Compose |

## Run It

**Prerequisites:** Docker Desktop running.

1. Create your environment file and fill in your own values:

   ```
   cp .env.example .env
   ```

   Compose refuses to start if `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, `REDIS_PASSWORD`, or `JWT_SECRET` is empty.

2. Start the stack:

   ```
   docker-compose up -d --build
   ```

3. Optional database and Redis UIs:

   ```
   docker-compose --profile tools up -d
   ```

4. Stop everything:

   ```
   docker-compose down
   ```

| Service | URL / Port |
| --- | --- |
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Health check | `http://localhost:8080/actuator/health` |
| MySQL (from host) | `localhost:3307` |
| MailHog inbox | `http://localhost:8025` |
| Redis Commander (tools profile) | `http://localhost:8081` |
| phpMyAdmin (tools profile) | `http://localhost:8082` |

## Performance: Redis Caching

k6 load test on `GET /api/books`, run locally on Docker: 50 virtual users for 60 seconds. Set `APP_CACHE_ENABLED=true` for the cache-on run and `false` for the baseline.

| | Cache off | Cache on |
| --- | --- | --- |
| Avg latency | 1.53 s | 208.86 ms |
| p95 latency | 2.90 s | 306.85 ms |
| Throughput | 30 req/s | 160.7 req/s |
| MySQL row reads per request | ~40,000 | ~330 |

With Redis enabled, p95 latency dropped about 9x and throughput rose about 5x on this endpoint. Numbers come from a single local run, so treat them as indicative, not as production figures.

## Testing

```
./mvnw test
```

- `@DataJpaTest` repository tests run against H2
- `@WebMvcTest` controller tests use `MockMvc`
- `@SpringBootTest` integration tests cover the full stack

## Project Notes

- Hibernate first-level cache staleness after bulk `@Modifying` updates is handled with `entityManager.flush()` and `clear()` in integration tests.
- The H2/MySQL dialect conflict in tests came from `spring.jpa.properties.hibernate.dialect` overriding the test profile.
