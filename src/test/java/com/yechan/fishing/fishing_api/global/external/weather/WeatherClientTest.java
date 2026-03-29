package com.yechan.fishing.fishing_api.global.external.weather;

import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.domain.weather.dto.WeatherResponse;
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

class WeatherClientTest {

    private MockWebServer server;
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();

        OpenWeatherApiProperties props = new OpenWeatherApiProperties();
        props.setBaseUrl(server.url("/").toString());
        props.setKey("test-key");

        WebClient webClient = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();

        weatherClient = new WeatherClient(webClient, props);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void getWeather_returnsMappedResponse() throws Exception {
        server.enqueue(jsonResponse("""
                {
                  "dt": 1710000000,
                  "main": { "temp": 21.3, "feels_like": 19.8, "humidity": 61 },
                  "weather": [{ "main": "Clouds", "description": "few clouds" }],
                  "wind": { "speed": 3.5, "deg": 180 },
                  "clouds": { "all": 20 },
                  "sys": { "sunrise": 1710000100, "sunset": 1710043200 }
                }
                """));

        WeatherResponse result = weatherClient.getWeather(37.5, 127.0);
        RecordedRequest request = server.takeRequest();

        assertEquals("/data/2.5/weather?lat=37.5&lon=127.0&units=metric&appid=test-key", request.getPath());
        assertEquals(21.3, result.temperature(), 0.001);
        assertEquals("Clouds", result.weather());
        assertEquals(1710000100L, result.sunrise());
        assertEquals(1710043200L, result.sunset());
    }

    @Test
    void getGptWeatherContext_returnsMappedContext() {
        server.enqueue(jsonResponse("""
                {
                  "dt": 1710000000,
                  "main": { "temp": 21.3, "feels_like": 19.8, "humidity": 61 },
                  "weather": [{ "main": "Clouds", "description": "few clouds" }],
                  "wind": { "speed": 3.5, "deg": 180 },
                  "clouds": { "all": 20 },
                  "sys": { "sunrise": 1710000100, "sunset": 1710043200 }
                }
                """));

        GptWeatherContext result = weatherClient.getGptWeatherContext(37.5, 127.0);

        assertEquals(37.5, result.lat(), 0.001);
        assertEquals(127.0, result.lng(), 0.001);
        assertEquals(1710000000L, result.timestamp());
        assertEquals(19.8, result.feelsLike(), 0.001);
        assertEquals(61, result.humidity());
        assertEquals(3.5, result.windSpeed(), 0.001);
        assertEquals(180, result.windDeg());
        assertEquals(20, result.cloudiness());
        assertEquals("few clouds", result.weatherDesc());
    }

    @Test
    void getWeather_whenApiReturnsError_throwsWeatherApiError() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"message\":\"boom\"}"));

        FishingException ex = assertThrows(FishingException.class, () -> weatherClient.getWeather(37.5, 127.0));

        assertEquals(ErrorCode.WEATHER_API_ERROR, ex.getErrorCode());
    }

    @Test
    void getWeather_whenResponseIsInvalid_throwsInvalidDataError() {
        server.enqueue(jsonResponse("""
                {
                  "dt": 1710000000,
                  "main": null,
                  "weather": [],
                  "wind": { "speed": 3.5, "deg": 180 },
                  "clouds": { "all": 20 },
                  "sys": { "sunrise": 1710000100, "sunset": 1710043200 }
                }
                """));

        FishingException ex = assertThrows(FishingException.class, () -> weatherClient.getWeather(37.5, 127.0));

        assertEquals(ErrorCode.WEATHER_API_INVALID_DATA, ex.getErrorCode());
    }

    private MockResponse jsonResponse(String body) {
        return new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
