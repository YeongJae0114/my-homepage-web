# My Homepage Backend

## Local Development

The local profile uses Spring Boot Docker Compose support to run PostgreSQL for development.

Run `BackendApplication` from IntelliJ with the `local` profile. If no profile is provided, `local` is used by default.

Spring Boot reads `docker-compose.yaml` and starts the PostgreSQL container automatically when the application starts.

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
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
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
