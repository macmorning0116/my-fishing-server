# AGENTS.md

## 목적
- 이 문서는 `/Users/kim-yechan/Desktop/fishing-api` 에서 작업하는 AI 코딩 에이전트를 위한 작업 기준서입니다.
- 이 프로젝트는 Spring Boot 기반 백엔드이며, 분석 API, 검색 API, 지도/날씨 API, 향후 커뮤니티 API를 함께 다룹니다.

## 우선 원칙
- 빠른 구현보다 유지보수 가능한 구조와 명확한 도메인 경계를 우선합니다.
- 운영 기준 저장소는 PostgreSQL(RDS)이며, 검색은 OpenSearch를 사용합니다.
- 인증은 소셜 로그인 + JWT(access/refresh) 구조를 기본으로 가정합니다.

## 기술 맥락
- Java 17
- Spring Boot
- PostgreSQL
- OpenSearch
- 운영 이미지 저장소는 S3, 로컬 개발 환경은 파일 시스템 저장소를 사용합니다.

## 도메인 원칙
- 커뮤니티 기능은 `users`, `user_refresh_tokens`, `community_posts`, `community_post_images`, `community_post_likes`, `community_comments` 기준으로 설계합니다.
- 게시글 목록/지도 조회 성능을 위해 `community_posts` 에 `thumbnail_image_url`, `like_count`, `comment_count` 같은 집계/대표 컬럼을 둡니다.
- 좋아요 여부는 count 컬럼만 믿지 말고, 실제 기록 테이블 기준으로 판단합니다.

## 업로드 원칙
- 이미지 저장은 `ImageStorageService` 인터페이스로 추상화합니다.
- 로컬은 `LocalImageStorageService`, 운영은 `S3ImageStorageService` 로 분리합니다.
- 스토리지 구현을 바꾸더라도 DB에는 최종 `image_url` 만 저장하는 구조를 유지합니다.

## 인증 원칙
- 유저 식별 기준은 `(provider, provider_user_id)` 입니다.
- refresh token 은 별도 테이블에 저장하고, 로그아웃/폐기 가능해야 합니다.
- 지금 단계에서는 Redis를 필수로 가정하지 않습니다.

## 코드 작업 원칙
- 컨트롤러보다 서비스와 도메인 모델의 책임 분리를 먼저 확인합니다.
- 새 기능 추가 시 요청/응답 DTO와 테스트 범위를 함께 생각합니다.
- 설정값이나 인덱스명처럼 운영 영향이 있는 값은 하드코딩하지 말고 설정으로 분리합니다.

## 작업 전후 확인
- 백엔드 변경 후에는 `./gradlew spotlessCheck` 와 관련 테스트를 우선 실행합니다.
- 포맷 이슈가 있으면 `./gradlew spotlessApply` 로 정리한 뒤 다시 확인합니다.
- 빌드 또는 테스트가 실패하면 원인을 확인하고, 해결되지 않은 상태로 마무리하지 않습니다.
- 검색/인덱스 구조를 바꿀 때는 실제 조회 결과와 매핑 영향을 함께 확인합니다.

## 자주 쓰는 확인 항목
- 포맷 검사: `./gradlew spotlessCheck`
- 포맷 적용: `./gradlew spotlessApply`
- 테스트 실행: `./gradlew test`
- 커버리지 확인: `./gradlew test jacocoTestReport`
- OpenSearch 인덱스 변경 시 settings, mappings, analyze, search 결과를 함께 확인합니다.

## 커밋 메시지 규칙
- 예시: `feat: 지역 집계 검색 API 확장`
- 예시: `fix: 게시글 작성일 저장 기준 수정`
- 커밋 본문은 실제 줄바꿈으로 작성합니다.
- 커밋 본문 각 줄은 `- ` 로 시작합니다.
- `했습니다` 같은 서술형보다 `추가`, `수정`, `개선` 형태를 선호합니다.
