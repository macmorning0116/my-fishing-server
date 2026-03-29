package com.yechan.fishing.fishing_api.domain.weather.controller;

import com.yechan.fishing.fishing_api.domain.weather.dto.WeatherResponse;
import com.yechan.fishing.fishing_api.domain.weather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void getCurrentWeather_returnsWrappedSuccessResponse() throws Exception {
        given(weatherService.getCurrentWeather(eq(37.5), eq(127.0)))
                .willReturn(new WeatherResponse(22.5, "Clouds", 100L, 200L));

        mockMvc.perform(get("/v1/weather")
                        .param("lat", "37.5")
                        .param("lng", "127.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.temperature").value(22.5))
                .andExpect(jsonPath("$.data.weather").value("Clouds"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void getCurrentWeather_whenLatitudeIsOutOfRange_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/v1/weather")
                        .param("lat", "91")
                        .param("lng", "127.0"))
                .andExpect(status().isBadRequest());
    }
}
