# Backend Precedents

`PRECEDENTS.md`는 반복되는 판단 사례를 축적하는 장소다. 같은 유형의 사례가 3번 반복되면 `RULES.md`로 승격한다.

## 기록 형식

```md
## YYYY-MM-DD: 사례 제목

- 유형: controller-contract | module-boundary | storage-query | security | migration | testing | 기타
- 상황:
- 판단:
- 결과:
- 반복 카운트: 1/3
- 승격 상태: 후보 | 승격됨
- 관련 파일:
```

## 승격 기준

- 같은 유형과 같은 판단이 3번 반복되면 `RULES.md`로 승격한다.
- 단순히 비슷한 파일을 수정한 횟수가 아니라, 같은 의사결정이 반복되었는지를 기준으로 한다.
- 승격 후에도 원본 사례는 삭제하지 않고 `승격됨`으로 표시한다.

## 사례 목록

아직 기록된 사례가 없다.

## 2026-06-20: PostgreSQL upsert mapper 검증은 H2 분기 대신 컨테이너로 격리

- 유형: testing | storage-query
- 상황: H2 `MODE=PostgreSQL`에서도 MyBatis mapper의 `on conflict` upsert SQL이 syntax error로 실패했다.
- 판단: 운영 SQL을 H2용 `merge`로 분기하지 않고 PostgreSQL `on conflict` SQL을 단일 소스로 유지한다. 해당 upsert 동작 검증은 기본 H2 mapper 테스트에서 제외하고 `@Tag("container")` Testcontainers 테스트로 검증한다.
- 결과: `DatabaseIdProvider`와 mapper XML `databaseId` 분기를 제거하고, notification/favorite-count upsert 검증을 PostgreSQL 컨테이너 테스트로 이동했다.
- 반복 카운트: 1/3
- 승격 상태: 후보
- 관련 파일: `storage/db-core/src/main/resources/mybatis/mapper/NotificationMapper.xml`, `storage/db-core/src/main/resources/mybatis/mapper/AttractionMapper.xml`, `storage/db-core/src/test/java/com/ssafy/enjoytrip/storage/db/core/container/PostgresUpsertMapperContainerTest.java`
