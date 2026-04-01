# AI-Fishing API

> 사진 기반 낚시 포인트 분석, 위치/날씨 정보 제공, 조행 데이터 검색을 담당하는 API 서버입니다.  
> React 프론트엔드와 연동되며, AWS EC2 환경에서 Docker 기반으로 운영합니다.

<img width="180" alt="검색화면" src="https://github.com/user-attachments/assets/cd1c3d1a-d465-46ab-8c90-45b78b3bf7bc" />
<img width="180" alt="홈화면" src="https://github.com/user-attachments/assets/be31f968-35c9-4e6d-95b5-5593a91519ba" />
<img width="180" alt="분석중화면" src="https://github.com/user-attachments/assets/43342198-48d5-44fb-9b8f-c54009d9961b" />
<img width="180" alt="분석화면" src="https://github.com/user-attachments/assets/5f437d21-5164-4863-ad19-71abd4d38ffd" />

## 프로젝트 소개

AI-Fishing은 사용자의 현재 위치와 날씨, 업로드한 사진을 바탕으로 낚시 포인트를 분석하고,  
별도로 수집한 네이버 카페 조행 데이터를 검색해 실제 조행 사례까지 함께 탐색할 수 있도록 만든 서비스입니다.


- 사진 기반 낚시 포인트 분석 API 제공
- 위치 기반 주소 및 날씨 정보 제공
- OpenSearch 기반 조행 데이터 검색 API 제공
- 운영 환경 배포 및 테스트 자동화

## 주요 기능

### 1. 사진 기반 낚시 포인트 분석
- 업로드한 사진을 바탕으로 GPT가 포인트 특징, 공략 포인트, 추천 채비를 분석
- 위치 좌표와 현재 날씨를 함께 반영해 실제 낚시 상황에 가까운 결과 제공

### 2. 위치/주소/날씨 정보 제공
- 네이버 지도 Reverse Geocoding API로 현재 위치의 주소 정보 제공
- OpenWeather API로 현재 날씨를 조회해 분석 결과와 함께 전달

### 3. 조행 데이터 검색 API
- 네이버 카페 조행 게시글을 OpenSearch에 색인해 검색 API 제공
- 제목, 본문, 포인트, 지역, 게시판 기준 검색 지원
- `search_after` 기반 커서 페이지네이션 적용
- 영문/국문 혼용 표현, 낚시 은어·축약어 검색을 위한 동의어 사전 적용
  - 예: `bass ↔ 배스`, `프리 ↔ 프리리그`, `스베 ↔ 스윔베이트`

### 4. 운영 자동화
- GitHub Actions에서 테스트 성공 시에만 Docker 이미지를 빌드/배포
- GHCR에 이미지를 푸시한 뒤 EC2에서 `docker compose`로 서비스 갱신

## 주요 기술 포인트

- **Spring Boot + WebClient**
  외부 API 연동과 OpenSearch 검색 요청을 일관된 방식으로 구성
- **OpenSearch 검색 인프라 연동**
  검색 전용 인덱스를 통해 조행 데이터 검색 API 구현
- **동의어 사전 기반 검색 품질 개선**
  Amazon OpenSearch Service custom package + S3 기반 동의어 사전 적용
- **테스트 기반 배포**
  GitHub Actions에서 테스트 통과 후에만 배포
- **JaCoCo 커버리지 리포트**
  현재 라인 커버리지 기준 91.11%

## 아키텍처

<img width="800" alt="image" src="https://github.com/user-attachments/assets/92406d4e-3458-4189-bf1d-28c6c9ed1c4b" />



구성 요약:

- `Frontend (React)` → 사용자 UI 제공
- `Backend (Spring Boot)` → 분석/지도/날씨/검색 API 제공
- `OpenSearch` → 조행 데이터 검색 인덱스
- `Crawler + RDS` → 조행 데이터 수집 및 원본 저장
- `S3 + OpenSearch custom package` → 동의어 사전 관리

## API 개요

### 분석 API
- `POST /v1/analysis/photo`
- 사진 + 좌표를 입력받아 포인트 분석 결과 반환

### 지도 API
- `GET /v1/map/reverse-geocode`
- 위도/경도를 입력받아 주소 정보 반환

### 날씨 API
- `GET /v1/weather`
- 위도/경도를 입력받아 현재 날씨 정보 반환

### 검색 API
- `GET /v1/search/posts`
- 조행 게시글 검색 결과 반환

주요 쿼리 파라미터:

- `q`: 검색어
- `boardKey`: 게시판 키 필터
- `cursor`: 다음 페이지 커서
- `size`: 페이지 크기(기본 20, 최대 100)

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.4.1
- Spring Web / WebFlux
- Lombok

### External API / Search
- OpenAI API
- Naver Maps API
- OpenWeather API
- Amazon OpenSearch Service

### Infrastructure / DevOps
- AWS EC2
- Docker
- Docker Compose
- Nginx
- GitHub Actions
- GHCR

### Test
- JUnit 5
- Spring Boot Test
- Mockito
- MockWebServer
- JaCoCo

## 환경 변수

로컬 또는 운영 환경에서 아래 값을 설정해야 합니다.

```properties
NAVER_API_KEY_ID=
NAVER_API_KEY=

OPENWEATHER_API_KEY=

OPENAI_API_KEY=

OPENSEARCH_BASE_URL=
OPENSEARCH_INDEX_NAME=fishing_articles_v2
```



## 로컬 실행

```bash
./gradlew bootRun
```

기본 포트:
- `8080`

## 테스트 및 커버리지

테스트 실행:

```bash
./gradlew test
```

JaCoCo 리포트 생성:

```bash
./gradlew test jacocoTestReport
```



현재 기준:
- **Line Coverage: 91.11%**

## 배포

배포 파이프라인은 GitHub Actions로 구성되어 있습니다.

동작 방식:

1. `main` 브랜치에 push
2. Gradle 테스트 실행
3. Docker 이미지 빌드
4. GHCR 푸시
5. EC2 접속 후 `docker compose pull app`
6. `docker compose up -d app`



## 디렉터리 구조

```text
fishing-api/
├─ gradle/
├─ src/
│  ├─ main/
│  │  ├─ java/com/yechan/fishing/fishing_api/
│  │  │  ├─ domain/
│  │  │  │  ├─ analysis/
│  │  │  │  ├─ map/
│  │  │  │  ├─ search/
│  │  │  │  └─ weather/
│  │  │  └─ global/
│  │  │     ├─ config/
│  │  │     ├─ exception/
│  │  │     ├─ external/
│  │  │     ├─ logging/
│  │  │     └─ response/
│  │  └─ resources/
│  └─ test/
│     └─ java/
├─ .github/
│  └─ workflows/
│     └─ deploy.yml
├─ Dockerfile
├─ build.gradle
└─ README.md
```

