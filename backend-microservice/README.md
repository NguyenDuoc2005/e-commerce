# Backend Microservice

Spring Cloud microservice workspace for migrating the existing monolith in `../BE`.

Do not edit `../BE`. Move logic service by service and keep public API behavior compatible through `api-gateway`.

## Modules

- `discovery-server`: Netflix Eureka server.
- `api-gateway`: Spring Cloud Gateway public entrypoint.
- `common-lib`: shared response/exception DTOs only; no shared JPA entities.
- `auth-service`: JWT/OAuth2/login/register.
- `user-service`: customer and staff.
- `catalog-service`: products, product attributes, and current product-detail stock.
- `promotion-service`: vouchers and product promotions.
- `order-service`: orders, invoice history, checkout and VNPay in version 1.
- `cart-service`: shopping cart.
- `notification-service`: async email/event handling.

## Migrated API surface

### auth-service

- `POST /api/v1/auth/login`: customer login, returns `AuthTokens`.
- `POST /api/v1/auth/login-admin`: staff/admin login, returns `AuthTokens`.
- `PUT /api/v1/auth/register`: customer registration with the same field names as the monolith.
- `POST /api/v1/auth/change-password`: customer password change. It still accepts the old session email path and also supports `Authorization: Bearer <token>` so it works behind the gateway.

## Run the entire backend with Docker Compose

This is an alternative to `run-all.ps1`: every backend service runs in a
container. Stop the local Java services first to avoid port conflicts. Run the
following commands from `backend-microservice/`.

Copy `.env.example` to `.env`, then replace both placeholders with independently
generated secrets **before** running Compose. For example, in PowerShell:

```powershell
Copy-Item .env.example .env
function New-ComposeServiceCredential {
    $bytes = New-Object byte[] 32
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $generator.GetBytes($bytes)
    } finally {
        $generator.Dispose()
    }
    [Convert]::ToBase64String($bytes)
}
$composeCredentials = @(
    "SECURITY_GATEWAY_TOKEN=$(New-ComposeServiceCredential)"
    "SECURITY_INTERNAL_SERVICE_TOKEN=$(New-ComposeServiceCredential)"
)
Set-Content -LiteralPath .env -Value $composeCredentials -Encoding ASCII
Remove-Variable composeCredentials
docker compose up -d --build
```

Do not overwrite an existing `.env` when reusing an environment: the example
above initializes a new environment. `.env` is Git-ignored and excluded from
Docker build contexts; commit only `.env.example`. Do not use its placeholders
as actual credentials. Shell environment variables with the same names override
the values in `.env`; unset them first if you intend to use the file's values.

`TrustedRequestFilter` requires both values to be nonblank, with no additional
length or encoding constraint. The example uses the same secure 32-byte Base64
generation method as `run-all.ps1`. The two launch modes can use different
credentials; all containers within one Compose deployment must receive the same
pair. The nine downstream services (auth, user, catalog, promotion, cart, order,
notification, seller and payout) use the filter. API gateway also needs the pair
to forward trusted gateway/internal requests; discovery and infrastructure do
not need it. Blank credentials prevent the affected Spring applications from
starting, not merely cause a warning.

Inspect startup and application health:

```powershell
docker compose ps -a
docker compose logs --tail 100
Invoke-RestMethod http://localhost:8761/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/health
```

Wait for all 11 backend applications to report `UP` and for the 10 clients to
register with Eureka. `Up` in `docker compose ps` alone is not proof of
application readiness; not every container declares a Docker healthcheck.
This command does not seed demo accounts or product data.

On a fresh MySQL volume, Compose creates `ecommerce_catalog` and loads the
canonical `catalog-service/src/main/resources/db/migration/manual/p1_product_domain_reset.sql`
directly through `/docker-entrypoint-initdb.d/10-catalog-schema.sql`. This is the
same catalog schema loaded by `reset-demo-databases.ps1`; `init-databases.ps1`
only creates databases. Catalog keeps `ddl-auto=validate`. The other database
services keep their existing `ddl-auto=update` defaults and JDBC database
auto-creation; their application startup creates their mapped tables. No legacy
M10 migration or demo seed is applied during Compose initialization.

Database services wait for MySQL's final TCP server to become healthy, after
initialization completes. Kafka Connect waits for a successful broker API probe,
not just a running Kafka container. Allow several minutes for a cold start.
Initialization SQL runs only on an empty MySQL volume; an existing incompatible
schema requires a separately reviewed migration, not automatic table replacement.

Kafka UI is available at **http://localhost:8094** (host mapping `8094:8080`).
Its container port remains `8080`; host port `8090` is no longer used by this
Compose stack. Port `8091` remains available for the local payout-service runner.

For an explicitly requested clean reset only:

```powershell
docker compose down -v
docker compose up -d --build
```

**Warning:** `down -v` permanently deletes the Compose-managed MySQL, Kafka,
Elasticsearch, Prometheus and Grafana volumes. Omit `-v` when preserving data.

## Build from source

From repository root:

```powershell
BE\gradlew.bat -p backend-microservice clean build
```

Build was intentionally skipped on 2026-08-03 because it was too time-consuming for the current migration pass.
