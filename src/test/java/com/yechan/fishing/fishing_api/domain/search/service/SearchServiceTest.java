package com.yechan.fishing.fishing_api.domain.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostsRequest;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostsResponse;
import com.yechan.fishing.fishing_api.global.external.opensearch.OpenSearchProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchServiceTest {

    private MockWebServer server;
    private ObjectMapper objectMapper;
    private SearchService searchService;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        objectMapper = new ObjectMapper();

        OpenSearchProperties props = new OpenSearchProperties();
        props.setBaseUrl(server.url("/").toString());
        props.setIndexName("fishing_articles_v2");

        WebClient webClient = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .defaultHeader("Content-Type", "application/json")
                .build();

        searchService = new SearchService(webClient, props, objectMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void searchPosts_buildsQueryAndMapsResponse() throws Exception {
        server.enqueue(jsonResponse("""
                {
                  "hits": {
                    "total": { "value": 2 },
                    "hits": [
                      {
                        "_source": {
                          "article_id": "469820",
                          "title": "효마지 비온 뒤 출쫌,,",
                          "url": "https://example.com/articles/469820",
                          "author_name": "초보초보",
                          "published_at": "2026-04-01",
                          "date_text": "2026-04-01",
                          "board_key": "bass_walking",
                          "board_name": "배스 조행기(워킹조행)",
                          "species": "배스",
                          "region": "경상권",
                          "place": "효마지수지",
                          "access_status": "ok"
                        },
                        "sort": ["2026-04-01", "469820"]
                      },
                      {
                        "_source": {
                          "article_id": "469819",
                          "title": "예당지 4월 1일 아침",
                          "url": "https://example.com/articles/469819",
                          "author_name": "낚시광",
                          "published_at": "2026-04-01",
                          "date_text": "2026-04-01",
                          "board_key": "bass_walking",
                          "board_name": "배스 조행기(워킹조행)",
                          "species": "배스",
                          "region": "충청권",
                          "place": "예당저수지",
                          "access_status": "ok"
                        },
                        "sort": ["2026-04-01", "469819"]
                      }
                    ]
                  }
                }
                """));

        SearchPostsResponse response = searchService.searchPosts(
                new SearchPostsRequest(
                        "bass",
                        "bass_walking",
                        LocalDate.of(2026, 3, 28),
                        LocalDate.of(2026, 4, 1),
                        null,
                        2
                )
        );

        assertEquals(2, response.items().size());
        assertEquals(2L, response.total());
        assertEquals(2, response.size());
        assertNotNull(response.nextCursor());
        assertEquals("469820", response.items().get(0).articleId());
        assertEquals("2026-04-01", response.items().get(0).publishedAt());
        assertEquals(List.of("배스", "경상권", "효마지수지"), response.items().get(0).tags());

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/fishing_articles_v2/_search", request.getPath());

        JsonNode body = objectMapper.readTree(request.getBody().readUtf8());
        assertEquals(2, body.path("size").asInt());
        assertEquals("bass", body.path("query").path("bool").path("must").get(0).path("multi_match").path("query").asText());
        assertEquals("bass_walking", body.path("query").path("bool").path("filter").get(1).path("term").path("board_key").asText());
        assertEquals("2026-03-28", body.path("query").path("bool").path("filter").get(2).path("range").path("published_at").path("gte").asText());
        assertEquals("2026-04-01", body.path("query").path("bool").path("filter").get(2).path("range").path("published_at").path("lte").asText());
        assertEquals("published_at", body.path("sort").get(0).fieldNames().next());
    }

    @Test
    void searchPosts_whenCursorExists_sendsSearchAfterAndFallsBackToDateText() throws Exception {
        server.enqueue(jsonResponse("""
                {
                  "hits": {
                    "total": { "value": 1 },
                    "hits": [
                      {
                        "_source": {
                          "article_id": "469700",
                          "title": "스베 반응 좋네요",
                          "url": "https://example.com/articles/469700",
                          "author_name": "배서",
                          "date_text": "2026-04-01",
                          "board_key": "bass_boating",
                          "board_name": "배스 조행기(보팅조행)",
                          "species": "배스",
                          "access_status": "ok"
                        },
                        "sort": ["2026-04-01", "469700"]
                      }
                    ]
                  }
                }
                """));

        String cursor = Base64.getUrlEncoder()
                .encodeToString("[\"2026-04-01\",\"469820\"]".getBytes(StandardCharsets.UTF_8));

        SearchPostsResponse response = searchService.searchPosts(
                new SearchPostsRequest(null, null, null, null, cursor, 10)
        );

        assertEquals(1, response.items().size());
        assertEquals("2026-04-01", response.items().get(0).publishedAt());
        assertEquals(List.of("배스"), response.items().get(0).tags());
        assertNull(response.nextCursor());

        RecordedRequest request = server.takeRequest();
        JsonNode body = objectMapper.readTree(request.getBody().readUtf8());
        assertTrue(body.has("search_after"));
        assertEquals("2026-04-01", body.path("search_after").get(0).asText());
        assertEquals("469820", body.path("search_after").get(1).asText());
        assertEquals(1, body.path("query").path("bool").path("filter").size());
        assertTrue(body.path("query").path("bool").path("must").isMissingNode()
                || body.path("query").path("bool").path("must").isEmpty());
    }

    @Test
    void searchPosts_whenResponseBodyIsEmpty_returnsEmptyResponse() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(""));

        SearchPostsResponse response = searchService.searchPosts(
                new SearchPostsRequest(null, null, null, null, "not-a-valid-cursor", null)
        );

        assertTrue(response.items().isEmpty());
        assertEquals(0L, response.total());
        assertEquals(20, response.size());
        assertNull(response.nextCursor());
    }

    private MockResponse jsonResponse(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
