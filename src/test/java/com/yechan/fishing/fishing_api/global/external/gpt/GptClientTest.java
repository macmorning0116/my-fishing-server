package com.yechan.fishing.fishing_api.global.external.gpt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;


class GptClientTest {

    private MockWebServer server;
    private ObjectMapper objectMapper;
    private GptClient gptClient;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        objectMapper = new ObjectMapper();

        OpenAiProperties props = new OpenAiProperties();
        props.setModel("gpt5-2");

        WebClient webClient = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();

        gptClient = new GptClient(webClient, props, objectMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }


    @Test
    void analyze_success_requestShouldMatchSchema_andContainPromptAndImageDataUrl() throws Exception {
        server.enqueue(okGptResponse(minimalValidAnalysisJson()));

        MockMultipartFile image = jpegImage("fake-image-bytes");

        AnalysisResponse result = gptClient.analyze(image, dayWeather());
        assertNotNull(result);
        assertNotNull(result.points());
        assertEquals(2, result.points().size());

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/responses", request.getPath());

        JsonNode root = objectMapper.readTree(request.getBody().readUtf8());

        assertValidRequestSchema(root, "gpt5-2");

        String prompt = extractPrompt(root);
        assertTrue(prompt.contains("Output JSON only."));
        assertTrue(prompt.contains("currentTimeWindow: 주간"));

        assertTrue(extractImageUrl(root).startsWith("data:image/jpeg;base64,"));
        assertEquals("high", extractImageDetail(root));
    }

    @Test
    void analyze_whenMultipartContentTypeIsNull_shouldFallbackToImageJpeg() throws Exception {
        server.enqueue(okGptResponse(minimalValidAnalysisJson()));

        MockMultipartFile image = new MockMultipartFile(
                "image", "test.bin", null,
                "fake-image-bytes".getBytes(StandardCharsets.UTF_8)
        );

        AnalysisResponse result = gptClient.analyze(image, dayWeather());
        assertNotNull(result);

        RecordedRequest request = server.takeRequest();
        JsonNode root = objectMapper.readTree(request.getBody().readUtf8());

        assertTrue(extractImageUrl(root).startsWith("data:image/jpeg;base64"));
    }

    @Test
    void analyze_retry_secondRequestPromptShouldContainRetryHint() throws Exception {
        server.enqueue(okGptResponse("NOT_JSON_AT_ALL"));
        server.enqueue(okGptResponse(minimalValidAnalysisJson()));

        MockMultipartFile image = jpegImage("fake-image-bytes");

        AnalysisResponse result = gptClient.analyze(image, dayWeather());
        assertNotNull(result);
        assertEquals(2, server.getRequestCount());

        // 첫 요청 소비
        server.takeRequest();

        RecordedRequest secondRequest = server.takeRequest();
        JsonNode root2 = objectMapper.readTree(secondRequest.getBody().readUtf8());

        String prompt2 = extractPrompt(root2);
        assertTrue(prompt2.contains("Previous response violated rules"));
    }

    @Test
    void analyze_failsAfterMaxRetry_shouldThrowFishingException() throws Exception {
        server.enqueue(okGptResponse("BAD1"));
        server.enqueue(okGptResponse("BAD2"));
        server.enqueue(okGptResponse("BAD3"));

        MockMultipartFile image = jpegImage("fake-image-bytes");

        assertThrows(FishingException.class, () -> gptClient.analyze(image, dayWeather()));
        assertEquals(3, server.getRequestCount());
    }

    @Test
    void analyze_shouldUseNightWindow_whenNowIsOutsideSunriseSunset() throws Exception {
        server.enqueue(okGptResponse(minimalValidAnalysisJson()));

        MockMultipartFile image = jpegImage("fake-image-bytes");

        AnalysisResponse result = gptClient.analyze(image, nightWeather());
        assertNotNull(result);

        RecordedRequest request = server.takeRequest();
        JsonNode root = objectMapper.readTree(request.getBody().readUtf8());

        String prompt = extractPrompt(root);
        assertTrue(prompt.contains("currentTimeWindow: 야간"));
    }

    @Test
    void analyze_whenImageGetBytesThrows_shouldThrowGptApiError() throws Exception {
        MultipartFile badFile = Mockito.mock(MultipartFile.class);

        Mockito.when(badFile.getContentType()).thenReturn("image/jpeg");
        Mockito.when(badFile.getBytes()).thenThrow(new RuntimeException("boom"));

        FishingException ex = assertThrows(FishingException.class, () -> gptClient.analyze(badFile, dayWeather()));

        assertEquals(ErrorCode.GPT_API_ERROR, ex.getErrorCode());
    }


    // ------------------------------------- Schema Assertions / Extractors -------------------------------------

    private void assertValidRequestSchema(JsonNode root, String expectedModel) {
        JsonNode input0 = root.path("input").path(0);
        JsonNode content = input0.path("content");

        assertAll(
                () -> assertEquals(expectedModel, safeText(root.get("model"), "model")),
                () -> assertEquals("user", safeText(input0.get("role"), "input[0].role")),
                () -> assertTrue(content.isArray(), "input[0].content는 배열이어야 합니다."),
                () -> assertEquals(2, content.size(), "content는 text+image로 2개여야 합니다."),
                () -> assertNotNull(findContentByType(content, "input_text"), "input_text가 있어야 합니다."),
                () -> assertNotNull(findContentByType(content, "input_image"), "input_image가 있어야 합니다.")
        );
    }

    private String extractPrompt(JsonNode root) {
        JsonNode content = root.path("input").get(0).path("content");

        JsonNode textContent = findContentByTypeOrFail(content, "input_text");
        JsonNode textNode = textContent.get("text");

        assertNotNull(textNode, "input_text.text 필드가 존재해야 합니다.");
        assertTrue(textNode.isTextual());

        return textNode.textValue();
    }


    private String extractImageUrl(JsonNode root) {
        JsonNode content = root.path("input").get(0).path("content");

        JsonNode imageContent = findContentByType(content, "input_image");
        JsonNode urlNode = imageContent.get("image_url");

        assertNotNull(urlNode, "input_image.image_url 필드가 존재해야 합니다.");
        assertTrue(urlNode.isTextual(), "input_image_url은 문자열이어야 합니다.");

        return urlNode.textValue();
    }

    private String extractImageDetail(JsonNode root) {
        JsonNode content = root.path("input").path(0).path("content");

        JsonNode imageContent = findContentByTypeOrFail(content, "input_image");
        JsonNode detailNode = imageContent.get("detail");

        assertNotNull(detailNode, "input_image.detail 필드가 존재해야 합니다.");
        assertTrue(detailNode.isTextual());

        return detailNode.textValue();
    }

    private JsonNode findContentByType(JsonNode contentArray, String type) {
        if (contentArray == null || !contentArray.isArray()) return null;

        for (JsonNode node : contentArray) {
            JsonNode typeNode = node.get("type");
            if (typeNode != null && typeNode.isTextual() && type.equals(typeNode.textValue())) {
                return node;
            }
        }
        return null;
    }

    private JsonNode findContentByTypeOrFail(JsonNode contentArray, String type) {
        JsonNode found = findContentByType(contentArray, type);
        if (found == null) {
            fail("content에서 type= " + type + " 항목을 찾지 못했습니다.");
        }
        return found;
    }

    private String safeText(JsonNode node, String fieldName) {
        assertNotNull(node, fieldName + " 필드가 존재해야 합니다.");
        assertTrue(node.isTextual(), fieldName + "는 문자열이어야 합니다.");
        return node.textValue();
    }


    // ------------------------------------- Mock Server Response -------------------------------------

    private MockResponse okGptResponse(String outputText) throws Exception {
        String body = """
                {
                    "output" : [
                        { "content" : [ { "type": "output_text", "text": %s } ] }
                    ]
                }
                """.formatted(objectMapper.writeValueAsString(outputText));

        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    // ------------------------------------- Test Fixtures -------------------------------------

    private MockMultipartFile jpegImage(String bytes) {
        return new MockMultipartFile(
                "image", "test.jpg", "image/jpeg",
                bytes.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String minimalValidAnalysisJson() {
        // AnalysisResponse 구조에 맞게 최소 필드 + points 2개 구성
        return """
                {
                  "summary": "요약입니다.",
                  "points": [
                    { "x": 0.30, "y": 0.70, "radius": 0.12, "reason": "이유1입니다." },
                    { "x": 0.60, "y": 0.75, "radius": 0.10, "reason": "이유2입니다." }
                  ],
                  "tackle": "대상어: 배스(주)/(부) | 소프트: 스트레이트4인치 네꼬 훅1 싱커/헤드2g | 하드/메탈: 스푼7g 슬로우폴 | 라인/로드: 나일론8lb ML6.6ft",
                  "strategy": "① ...\\\\n② ...\\\\n③ ...\\\\n④ ..."
                }
                """;
    }

    private GptWeatherContext dayWeather() {
        return new GptWeatherContext(
                37.0,        // lat
                127.0,       // lng
                1700000000L, // timestamp (dt)
                10.0,        // temperature (air temp)
                9.0,         // feelsLike
                50,          // humidity
                2.0,         // windSpeed
                90,          // windDeg
                10,          // cloudiness
                "Clear",     // weatherMain
                "clear sky", // weatherDesc
                1699990000L, // sunrise
                1700030000L  // sunset
        );
    }

    private GptWeatherContext nightWeather() {
        // now가 sunrise 이전 -> 야간
        return new GptWeatherContext(
                37.0,
                127.0,
                1699980000L, // timestamp(now) < sunrise
                8.0,
                7.0,
                60,
                1.5,
                120,
                30,
                "Clouds",
                "broken clouds",
                1699990000L, // sunrise
                1700030000L  // sunset
        );
    }


}