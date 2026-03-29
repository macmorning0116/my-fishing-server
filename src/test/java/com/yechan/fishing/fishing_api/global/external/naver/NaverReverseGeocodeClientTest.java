package com.yechan.fishing.fishing_api.global.external.naver;

import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NaverReverseGeocodeClientTest {

    private MockWebServer server;
    private NaverReverseGeocodeClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();

        client = new NaverReverseGeocodeClient(webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void reverseGeocode_returnsExtractedAddress() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "results": [
                            {
                              "region": {
                                "area1": { "name": "서울특별시" },
                                "area2": { "name": "중구" },
                                "area3": { "name": "태평로1가" }
                              }
                            }
                          ]
                        }
                        """));

        NaverAddress result = client.reverseGeocode(37.5665, 126.9780);
        RecordedRequest request = server.takeRequest();

        assertEquals("/map-reversegeocode/v2/gc?coords=126.978,%2037.5665&output=json&orders=legalcode,admcode,addr,roadaddr", request.getPath());
        assertEquals("서울특별시", result.sido());
        assertEquals("중구", result.sigungu());
        assertEquals("태평로1가", result.dong());
    }

    @Test
    void reverseGeocode_whenResultsMissing_throwsNaverApiError() {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("{\"results\":[]}"));

        FishingException ex = assertThrows(FishingException.class, () -> client.reverseGeocode(37.5665, 126.9780));

        assertEquals(ErrorCode.NAVER_API_ERROR, ex.getErrorCode());
    }
}
