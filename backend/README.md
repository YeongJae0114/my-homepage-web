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

Local monitoring tools are also started by Docker Compose:

```text
Prometheus: http://localhost:9090
Grafana: http://localhost:3001
Grafana login: admin / admin
Spring metrics: http://localhost:8080/actuator/prometheus
```

Prometheus scrapes the local Spring application through `host.docker.internal:8080`, and Grafana is preconfigured with Prometheus as its default datasource.

To add real servers to the same Prometheus setup, copy the example target file and replace hosts:

```bash
cp monitoring/prometheus/real-servers.example monitoring/prometheus/targets/real-servers.yml
```

Each real server should expose a metrics endpoint such as `node_exporter` on port `9100`. Keep Prometheus and Grafana private; expose only the Spring API and frontend publicly.

The Spring backend also stores public monitoring snapshots for the homepage. It collects enabled LAN servers every 10 minutes and keeps only the latest 30 days of snapshots.

```text
Configured LAN targets:
raspi-observe  192.168.1.104:9100
raspi-redis    192.168.1.103:9100
lab-mini       192.168.1.102:9100
lab-main       192.168.1.105:9100
raspi-db       192.168.1.106:9100
```

Collection settings:

```text
MONITORING_COLLECTION_ENABLED=true
MONITORING_COLLECTION_FIXED_DELAY=10m
MONITORING_COLLECTION_INITIAL_DELAY=30s
MONITORING_COLLECTION_REQUEST_TIMEOUT=3s
MONITORING_RETENTION_PERIOD=30d
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

## Production Docker Deployment

CI/CD deploys only the Spring backend container. PostgreSQL should already be
running on the same server as a separate container.

Create one shared Docker network on the server and attach PostgreSQL, Nginx,
and the Spring backend to it:

```bash
docker network create my-homepage-net
```

The production compose file is:

```text
compose.prod.yml
```

The deploy workflow writes `/path/to/deploy/.env.production` on the server
from GitHub Actions secrets:

```text
DB_URL=jdbc:postgresql://postgres:5432/my_homepage
DB_USERNAME=secret-value
DB_PASSWORD=secret-value
APP_CORS_ALLOWED_ORIGINS=https://your-domain.example
APP_FRONTEND_BASE_URL=https://your-domain.example
MONITORING_COLLECTION_ENABLED=false
```

Use the PostgreSQL container name in `DB_URL`. For example, if your DB
container is named `my-homepage-postgres`, use:

```text
DB_URL=jdbc:postgresql://my-homepage-postgres:5432/my_homepage
```

Nginx can proxy to the backend through the same Docker network:

```nginx
location /api/ {
    proxy_pass http://my-homepage-backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

GitHub repository secrets required for automatic deployment:

```text
BACKEND_HOST
BACKEND_USER
BACKEND_SSH_KEY
BACKEND_PORT
BACKEND_DEPLOY_PATH
GHCR_USERNAME
GHCR_TOKEN
DB_URL
DB_USERNAME
DB_PASSWORD
APP_CORS_ALLOWED_ORIGINS
APP_FRONTEND_BASE_URL
MONITORING_COLLECTION_ENABLED
```

`BACKEND_PORT` is optional when SSH uses port `22`.
`GHCR_TOKEN` should be a GitHub token that can read packages from GitHub
Container Registry. The deploy workflow copies `compose.prod.yml` to
`BACKEND_DEPLOY_PATH`, writes `.env.production` from GitHub Actions secrets,
pulls the new backend image, and restarts only the Spring backend container.
