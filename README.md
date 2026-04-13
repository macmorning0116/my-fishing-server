# Basstargram API

> 사진 기반 낚시 포인트 분석, 위치/날씨 정보 제공, 조행 데이터 검색, 커뮤니티 피드를 담당하는 API 서버입니다.  
> React 프론트엔드와 연동되며, AWS EC2 환경에서 Docker 기반으로 운영합니다.

<img width="180" alt="검색화면" src="https://github.com/user-attachments/assets/cd1c3d1a-d465-46ab-8c90-45b78b3bf7bc" />
<img width="180" alt="홈화면" src="https://github.com/user-attachments/assets/be31f968-35c9-4e6d-95b5-5593a91519ba" />
<img width="180" alt="분석화면" src="https://github.com/user-attachments/assets/5f437d21-5164-4863-ad19-71abd4d38ffd" />
<img width="180" alt="피드화면" src="https://github.com/user-attachments/assets/6d159611-6f7c-41b1-8227-d531303de3cf" />


## 프로젝트 소개

Basstargram은 사용자의 현재 위치와 날씨, 업로드한 사진을 바탕으로 낚시 포인트를 분석하고,  
별도로 수집한 네이버 카페 조행 데이터를 검색해 실제 조행 사례까지 함께 탐색할 수 있도록 만든 서비스입니다.  
커뮤니티 피드를 통해 유저 간 조행 기록을 공유하고, 소셜 로그인으로 간편하게 이용할 수 있습니다.

- 사진 기반 낚시 포인트 분석 API 제공
- 위치 기반 주소 및 날씨 정보 제공 (Caffeine 캐시 적용)
- OpenSearch 기반 조행 데이터 검색 API 제공
- 커뮤니티 피드 (게시글/댓글/좋아요/신고 CRUD)
- 소셜 로그인 (카카오/구글 OAuth)
- 이미지 업로드 시 썸네일 자동 생성
- 운영 환경 배포 및 테스트 자동화

## 주요 기능

### 1. 사진 기반 낚시 포인트 분석
- 업로드한 사진을 바탕으로 GPT가 포인트 특징, 공략 포인트, 추천 채비를 분석
- 위치 좌표와 현재 날씨를 함께 반영해 실제 낚시 상황에 가까운 결과 제공

### 2. 위치/주소/날씨 정보 제공
- 네이버 지도 Reverse Geocoding API로 현재 위치의 주소 정보 제공
- OpenWeather API로 현재 날씨를 조회해 분석 결과와 함께 전달
- Caffeine 캐시 적용 (5분 TTL)으로 외부 API 호출 최소화

### 3. 조행 데이터 검색 API
- 네이버 카페 조행 게시글을 OpenSearch에 색인해 검색 API 제공
- 제목, 본문, 포인트, 지역, 게시판 기준 검색 지원
- `search_after` 기반 커서 페이지네이션 적용
- 영문/국문 혼용 표현, 낚시 은어·축약어 검색을 위한 동의어 사전 적용
  - 예: `bass ↔ 배스`, `프리 ↔ 프리리그`, `스베 ↔ 스윔베이트`

### 4. 커뮤니티 피드
- 게시글 CRUD (생성/조회/수정/삭제) — 모든 필드 + 이미지 편집 지원
- 댓글/대댓글 CRUD + 소프트 삭제
- 좋아요/취소 (atomic 카운트 업데이트)
- 신고 (중복 방지, 자동 숨김 임계값)
- 이미지 업로드 시 썸네일 자동 생성 (400px, JPEG 80%, Thumbnailator)
- S3 이미지 병렬 업로드 (CompletableFuture)

### 5. 소셜 로그인
- 카카오/구글 OAuth 2.0 지원
- JWT Access Token + HttpOnly Refresh Token 쿠키
- 신규 유저 PENDING → 프로필 설정 후 ACTIVE 전환
- 닉네임 변경 30일 쿨다운

### 6. API 성능 최적화
- 피드 목록 경량 DTO (CommunityPostSummaryItem, 19→12 필드)
- JOIN FETCH로 N+1 쿼리 해결 (목록 23쿼리 → 2쿼리)
- 커서 기반 페이지네이션 (게시글/댓글)
- 복합 인덱스 (visibility_status+id, user+visibility+id, refresh_token)
- 불필요한 쿼리 제거 (좋아요 COUNT, 목록 이미지 로드)

### 7. 코드 품질
- Spotless (Google Java Format 자동 포맷)
- Checkstyle (네이밍/import/메서드 길이 규칙 검사)
- Flyway DB 마이그레이션 (V1~V6)

### 8. 운영 자동화
- GitHub Actions에서 테스트 성공 시에만 Docker 이미지를 빌드/배포
- GHCR에 이미지를 푸시한 뒤 EC2에서 `docker compose`로 서비스 갱신

## 주요 기술 포인트

- **Spring Boot + WebClient**
  외부 API 연동과 OpenSearch 검색 요청을 일관된 방식으로 구성
- **OpenSearch 검색 인프라 연동**
  검색 전용 인덱스를 통해 조행 데이터 검색 API 구현
- **동의어 사전 기반 검색 품질 개선**
  Amazon OpenSearch Service custom package + S3 기반 동의어 사전 적용
- **JWT 인증 + OAuth 소셜 로그인**
  카카오/구글 OAuth 2.0, Access Token + HttpOnly Refresh Token 쿠키 기반 인증
- **이미지 처리 파이프라인**
  업로드 시 Thumbnailator로 썸네일 자동 생성, S3 병렬 업로드로 속도 최적화
- **API 성능 최적화**
  N+1 해결, 경량 DTO, 복합 인덱스, Caffeine 캐시로 응답 속도 개선
- **테스트 기반 배포**
  GitHub Actions에서 테스트 통과 후에만 배포
- **JaCoCo 커버리지 리포트**
  현재 라인 커버리지 기준 91.11%

## 아키텍처

<img width="800" alt="image" src="https://github.com/user-attachments/assets/92406d4e-3458-4189-bf1d-28c6c9ed1c4b" />

구성 요약:

- `Frontend (React)` → 사용자 UI 제공
- `Backend (Spring Boot)` → 분석/지도/날씨/검색/커뮤니티/인증 API 제공
- `PostgreSQL (RDS)` → 유저/게시글/댓글/좋아요/신고 데이터 저장
- `OpenSearch` → 조행 데이터 검색 인덱스
- `S3` → 이미지 저장 (원본 + 썸네일) + 동의어 사전 관리
- `Crawler + RDS` → 조행 데이터 수집 및 원본 저장

## API 개요

### 분석 API
- `POST /v1/analysis/photo` — 사진 + 좌표를 입력받아 포인트 분석 결과 반환

### 지도 API
- `GET /v1/map/reverse-geocode` — 위도/경도를 입력받아 주소 정보 반환

### 날씨 API
- `GET /v1/weather` — 위도/경도를 입력받아 현재 날씨 정보 반환

### 검색 API
- `GET /v1/search/posts` — 조행 게시글 검색 결과 반환
- `GET /v1/search/regions` — 지역별 게시글 수 반환

### 커뮤니티 API
- `GET /v1/community/posts` — 피드 목록 (커서 페이지네이션)
- `GET /v1/community/posts/{postId}` — 게시글 상세
- `POST /v1/community/posts` — 게시글 작성 (multipart/form-data)
- `PUT /v1/community/posts/{postId}` — 게시글 수정 (모든 필드 + 이미지)
- `DELETE /v1/community/posts/{postId}` — 게시글 삭제 (소프트 삭제)
- `GET /v1/community/posts/{postId}/comments` — 댓글 목록 (커서 페이지네이션)
- `POST /v1/community/posts/{postId}/comments` — 댓글 작성
- `PUT /v1/community/comments/{commentId}` — 댓글 수정
- `DELETE /v1/community/comments/{commentId}` — 댓글 삭제
- `POST /v1/community/posts/{postId}/likes` — 좋아요
- `DELETE /v1/community/posts/{postId}/likes` — 좋아요 취소
- `POST /v1/community/posts/{postId}/reports` — 게시글 신고
- `POST /v1/community/comments/{commentId}/reports` — 댓글 신고

### 인증 API
- `GET /v1/auth/{provider}/authorize-url` — OAuth 인증 URL 반환
- `POST /v1/auth/{provider}/code` — 인가 코드로 JWT 발급
- `POST /v1/auth/refresh` — Access Token 갱신
- `POST /v1/auth/logout` — 로그아웃

### 유저 API
- `GET /v1/users/me` — 내 정보 조회
- `POST /v1/users/me/profile` — 프로필 설정 (닉네임)
- `PUT /v1/users/me/nickname` — 닉네임 변경
- `POST /v1/users/me/profile-image` — 프로필 사진 변경
- `GET /v1/users/{userId}` — 유저 프로필 조회
- `GET /v1/users/nickname/check` — 닉네임 중복 확인

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.4.1
- Spring Web / WebFlux
- Spring Data JPA + Flyway
- Hibernate Spatial (PostGIS)
- Lombok
- Caffeine Cache
- Thumbnailator

### Auth
- JWT (jjwt)
- OAuth 2.0 (카카오, 구글)

### External API / Search
- OpenAI API
- Naver Maps API
- OpenWeather API
- Amazon OpenSearch Service

### Storage
- PostgreSQL 16 (RDS) + PostGIS
- Amazon S3

### Code Quality
- Spotless (Google Java Format)
- Checkstyle (Google Style 기반)
- JaCoCo

### Infrastructure / DevOps
- AWS EC2 (t3.small)
- Docker / Docker Compose
- Nginx
- GitHub Actions
- GHCR

### Test
- JUnit 5
- Spring Boot Test
- Mockito
- MockWebServer
- JaCoCo

#### 테스트 실행
./gradlew test

#### 코드 포맷
./gradlew spotlessApply

#### 코드 컨벤션 검사
./gradlew checkstyleMain

#### JaCoCo 리포트
./gradlew test jacocoTestReport

