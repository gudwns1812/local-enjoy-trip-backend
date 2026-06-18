# storage:db-core Module Rules

이 파일은 `storage/db-core` 안의 storage contract, MyBatis mapper/XML, migration, db-core 테스트를 수정할 때 적용한다. 루트 `CONSTITUTION.md`가 항상 우선하고, 이 파일은 `RULES.md`와 `storage/AGENTS.md`를 db-core 기준으로 더 구체화한다.

## 책임 경계

- `storage:db-core`는 storage Record contract, MyBatis mapper interface/XML, type handler, Flyway migration, database configuration만 소유한다.
- web/controller, worker ingress, domain service/application flow, external API client 코드를 두지 않는다.
- core-api/batch service는 필요한 mapper와 storage Record를 직접 사용한다. db-core에 core-api domain 변환 책임을 넣지 않는다.
- storage contract 이름은 `*Record`로 유지한다. 사용하지 않는 `*Entity`, `*Row` 이중 모델을 남기지 않는다.

## Record 규칙

- storage Record는 MyBatis 결과 매핑용 Java contract다.
- 공통 감사 필드가 있으면 `BaseRecord`를 상속한다. 별도 이유 없이 `createdAt`, `updatedAt`을 각 Record에 중복 선언하지 않는다.
- JPA annotation, Spring Data auditing callback, repository adapter 계층을 추가하지 않는다.
- core-api domain model을 반환하는 `toModel`/`toDomain` 메서드를 Record에 만들지 않는다.
- Lombok 생성자는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 기본으로 사용한다.

## MyBatis SQL 규칙

- persistence SQL은 mapper XML에 둔다. service에서 SQL 문자열을 조립하지 않는다.
- mapper method 인자가 2개 이상이면 XML에서 쓰는 이름과 같은 `@Param`을 반드시 붙인다.
- PostGIS, pg_vector, `on conflict`, `returning`, 동적 조건, projection은 XML에 명시한다.
- schema 변경은 Flyway migration으로 남긴다.

## 테스트 규칙

- 일반 mapper 동작 검증은 H2 인메모리 DB 테스트로 작성한다.
  - 위치: `src/test/java/.../mybatis/h2`
  - fixture schema: `src/test/resources/h2/mapper-schema.sql`
  - Docker 없이 `:storage:db-core:test`에서 실행되어야 한다.
- Testcontainers는 H2로 의미 있게 검증하기 어려운 PostGIS/pg_vector 경로에만 사용한다.
  - 위치: `src/test/java/.../container`
  - `@Tag("container")`는 공통 support에 두고, 실제 테스트 클래스에는 필요한 `@Tag("postgis")`, `@Tag("pgvector")`를 붙인다.
  - 별도 sourceSet을 만들지 않는다.
  - Testcontainers 연결은 Spring Boot `@ServiceConnection` 기반으로 유지한다.
  - 기본 `test`는 container/slow/postgis/pgvector tag를 제외하고, 필요할 때만 `-PincludeContainerTests=true`로 명시 실행한다.
- PostGIS/pg_vector와 무관한 mapper를 컨테이너 테스트에 추가하지 않는다.
- 새 JUnit 테스트 메서드는 `@DisplayName`에 한글로 검증 의도를 적는다.

## 검증 기준

- db-core 일반 변경: `./gradlew :storage:db-core:test`
- container 경계 변경: Docker가 정상일 때 `./gradlew :storage:db-core:test -PincludeContainerTests=true`
- core-api 호출 경로까지 영향이 있으면 `./gradlew :core:core-api:check`도 실행한다.
- Docker/외부 런타임 문제로 container 검증을 못 하면 실패 원인과 대체 검증을 완료 보고에 남긴다.
