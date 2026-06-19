# External Module Rules

## Role

- `external` is kept as an independent module boundary and must not depend on `core-api` or `core`.
- API-facing outbound integration concrete clients, configuration, and neutral result DTOs live here under `com.ssafy.enjoytrip.external.*`; batch-only outbound implementations live in `batch`.
- Do not add source code here that imports `com.ssafy.enjoytrip.core.*`, storage Records/MyBatis types, web DTOs, or core domain models.
- Public client methods should return neutral external result DTOs. Core-api services are responsible for mapping those DTOs into domain models directly at the service call path.
- Do not add core-owned interface/port/gateway contracts for this module to implement.

## Verification

- Run `./gradlew :external:check` after modifying this module.
- If API-facing outbound client behavior changes, verify it through `:core:core-api:check`; if batch-only outbound behavior changes, verify it through `:batch:check`.
