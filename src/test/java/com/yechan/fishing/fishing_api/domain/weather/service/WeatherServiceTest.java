package com.yechan.fishing.fishing_api.domain.weather.service;

import com.yechan.fishing.fishing_api.domain.weather.dto.WeatherResponse;
import com.yechan.fishing.fishing_api.global.external.weather.WeatherClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void getCurrentWeather_delegatesToWeatherClient() {
        WeatherResponse response = new WeatherResponse(19.0, "Clear", 10L, 20L);
        given(weatherClient.getWeather(37.5, 127.0)).willReturn(response);

        WeatherResponse result = weatherService.getCurrentWeather(37.5, 127.0);

        assertSame(response, result);
        then(weatherClient).should().getWeather(37.5, 127.0);
    }
}
