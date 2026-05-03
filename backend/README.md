# My Homepage Backend

## Local Development

The local profile uses Spring Boot Docker Compose support to run PostgreSQL for development.

Run `BackendApplication` from IntelliJ with the `local` profile. If no profile is provided, `local` is used by default.

Spring Boot reads `docker-compose.yaml` and starts the PostgreSQL container automatically when the application starts.

API documentation is available while the application is running:

```text
Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/api-docs
```

Default local database values:

```text
DB_URL=jdbc:postgresql://localhost:5432/my_homepage
DB_USERNAME=my_homepage
DB_PASSWORD=my_homepage
```

If your local values are different, add only the changed keys to IntelliJ:

```text
Edit Configurations > BackendApplication > Environment variables
```

Common local variables:

```text
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:postgresql://localhost:5432/my_homepage
DB_USERNAME=my_homepage
DB_PASSWORD=my_homepage
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=true
```

## Database Migration

This project uses Flyway instead of Hibernate DDL generation.

Migration files live in:

```text
src/main/resources/db/migration
```

Rules:

```text
V*_create_* files create tables and constraints.
V*_seed_* files insert initial read-only content.
spring.jpa.hibernate.ddl-auto stays validate.
```

When the local Docker database already has tables created by an older `ddl-auto` run, reset the local volume before first Flyway use:

```bash
docker compose down -v
```

## Dev Deployment

The `dev` profile does not use Spring Docker Compose.

In CI/CD or the deployment runtime, inject values as environment variables or secrets:

```text
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://dev-db-host:5432/my_homepage
DB_USERNAME=secret-value
DB_PASSWORD=secret-value
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
```

Do not commit real secrets to Git.
