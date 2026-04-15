# CLAUDE.md

Spring Boot + Java 17 + PostgreSQL 낚시 서비스 백엔드.
프론트엔드: `/Users/kim-yechan/Desktop/fishing-frontend`

## 필수 규칙
- 작업 완료 후 커밋 전 반드시 아래 순서로 검증:
  1. `./gradlew spotlessApply` (코드 포맷)
  2. `./gradlew checkstyleMain` (코드 컨벤션 검사)
  3. `./gradlew test` (테스트 실행)
  4. 실패 시 원인 분석 → 해결 후 재실행 → 통과 확인
- 검증 통과 전에는 절대 커밋하지 않는다

## 기술 스택
- Java 17, Spring Boot 3.4, Gradle 9
- PostgreSQL 16 (Docker: `fishing-crawler-postgres`, 포트 5433)
- Flyway, JPA + PostGIS, JWT, Lombok
- Spotless (코드 자동 포맷, google-java-format)
- Checkstyle (코드 컨벤션 검사, Google Style 기반)
- Caffeine Cache (날씨/지도 API 캐싱)

## 명령어
- 포맷: `./gradlew spotlessApply`
- 컨벤션 검사: `./gradlew checkstyleMain`
- 테스트: `./gradlew test`
- 컴파일: `./gradlew compileJava`
- 빌드: `./gradlew build`
- DB 접속: `docker exec fishing-crawler-postgres psql -U fishing fishing_api`

## 주요 디렉터리
- `domain/auth/` — OAuth 로그인, JWT, @CurrentUser
- `domain/user/` — 프로필 관리, 닉네임 변경(30일 쿨다운)
- `domain/community/` — 게시글/댓글/좋아요/신고 CRUD
- `domain/map/` — 네이버 역지오코딩
- `global/exception/` — ErrorCode, GlobalExceptionHandler
- `global/response/` — ApiResponse 래퍼
- `global/config/` — CORS, 필터, ArgumentResolver, Cache
- `resources/db/migration/` — Flyway SQL (V1~V6)
- `config/checkstyle/` — Checkstyle 규칙 (checkstyle.xml, suppressions.xml)

## Checkstyle 규칙 요약
- 네이밍: 클래스=UpperCamelCase, 메서드/변수=lowerCamelCase, 상수=UPPER_SNAKE_CASE
- import: star import 금지, 미사용 import 금지
- 메서드 50줄, 파일 500줄, 파라미터 7개 제한 (entity/dto/test 제외)
- 코딩: equals/hashCode 쌍, 문자열 == 비교 금지, 빈 구문 금지
- 규칙 커스터마이징: `config/checkstyle/checkstyle.xml` 수정
- 예외 추가: `config/checkstyle/suppressions.xml` 수정

## 인증 흐름
1. `GET /v1/auth/{provider}/authorize-url` → OAuth URL
2. `POST /v1/auth/{provider}/code` → JWT 발급
3. 신규 유저: PENDING → 프로필 설정 후 ACTIVE
4. Access Token: Bearer 헤더, Refresh Token: HttpOnly 쿠키

## 코드 작성 원칙
- ErrorCode: 3인자 생성자 `(httpStatus, code, message)`
- 카운터 업데이트: Repository의 atomic @Query 사용 (엔티티 메서드 X)
- 좋아요/신고: DB UNIQUE 제약으로 멱등성 보장
- Flyway 마이그레이션: 적용 후 수정 불가, 새 버전으로 추가
- PENDING 유저도 프로필 설정 엔드포인트는 접근 가능

## API 응답 형식
- 성공: `{ "success": true, "data": { ... } }`
- 실패: `{ "success": false, "error": { "code": "...", "message": "..." } }`
- GlobalExceptionHandler가 ErrorCode의 httpStatus로 HTTP 상태 코드 반환

## 주의사항
- @ConditionalOnBean + JPA Repository → 빈 타이밍 이슈 가능
- uploads/ 디렉터리는 gitignore 대상
- 코드 수정 후 반드시 `./gradlew spotlessApply` → `./gradlew checkstyleMain` 실행

## 커밋 규칙
- `feat: 기능 추가`, `fix: 버그 수정` 형태
- 서술형(`했습니다`) 대신 명사형(`추가`, `수정`, `개선`)
- 커밋 author는 반드시 `macmorning0116 <rladPcks980@gmail.com>` 사용
- 다른 계정으로 커밋 금지
