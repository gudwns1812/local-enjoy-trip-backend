# Local Enjoy Trip Backend

동네에 숨겨져 있는 장소를 공유하고 다른 이용자들이 위치를 보고 잘 찾아갈 수 있는 서비스 곳곳입니다.

## 사전 요구사항

- JDK 25 (`gradle/java-test-conventions.gradle` toolchain)
- Docker / Docker Compose (로컬 DB, Redis, MinIO 실행용)

## 구조

```
.
├── core/
│   ├── core-api/        # Spring Boot 주 실행 모듈 (API main: com.ssafy.enjoytrip.EnjoyTripApplication)
│   │                     # HTTP API와 scheduled/background job을 단일 entrypoint에서 함께 실행한다.
│   │                     # Web/API 코드: com.ssafy.enjoytrip.core.api.web.*
│   └── core-enum/       # core-api와 storage/db-core가 공유하는 enum 전용 모듈
├── storage/
│   └── db-core/         # MyBatis mapper/XML, storage Record, Flyway migration 인프라
├── external/             # 외부 API, AI, MinIO 등 outbound 연동 모듈
├── batch/                # 수동/offline Spring Batch runtime
├── support/
│   ├── logging/          # 로깅 런타임 지원 리소스
│   └── monitoring/       # 모니터링 런타임 지원 리소스
├── docs/                 # 설계/API/운영 문서
├── infra/                # 로컬 인프라 설정 (postgres-vector 이미지, caddy 등)
├── scripts/              # 개발/운영 보조 스크립트
└── gradle/               # Gradle wrapper 및 공통 컨벤션
```

## 실행

로컬 인프라 기동 (DB/Redis/MinIO):

```powershell
docker compose --env-file .env up -d
```

서버 실행:

```powershell
.\gradlew :core:core-api:bootRun
```

빌드/검증:

```powershell
.\gradlew :core:core-api:check
.\gradlew :storage:db-core:check
.\gradlew :core:core-api:bootJar
```

생성 결과:

- `core/core-api/build/libs/core-api-1.0.0-SNAPSHOT.jar`
- `core/core-api/build/docs/asciidoc/index.html`

필수 환경변수:

- `ENJOYTRIP_DB_URL`
- `ENJOYTRIP_DB_USER`
- `ENJOYTRIP_DB_PASSWORD`
- `ENJOYTRIP_TOUR_API_KEY` 또는 `TOUR_API_KEY`
- `EV_CHARGER_API_KEY` 또는 `ENJOYTRIP_EV_API_KEY`
