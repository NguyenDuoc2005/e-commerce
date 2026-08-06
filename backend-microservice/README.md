# Backend Microservice

Spring Cloud microservice workspace for migrating the existing monolith in `../BE`.

Do not edit `../BE`. Move logic service by service and keep public API behavior compatible through `api-gateway`.

## Modules

- `discovery-server`: Netflix Eureka server.
- `api-gateway`: Spring Cloud Gateway public entrypoint.
- `common-lib`: shared response/exception DTOs only; no shared JPA entities.
- `auth-service`: JWT/OAuth2/login/register.
- `user-service`: customer and staff.
- `catalog-service`: products and product attributes.
- `inventory-service`: stock ownership.
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

## Build

From repository root:

```powershell
BE\gradlew.bat -p backend-microservice clean build
```

Build was intentionally skipped on 2026-08-03 because it was too time-consuming for the current migration pass.

