package com.yechan.fishing.fishing_api.domain.weather.service;

import com.yechan.fishing.fishing_api.domain.weather.dto.WeatherResponse;
import com.yechan.fishing.fishing_api.global.external.weather.WeatherClient;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final WeatherClient weatherClient;

    public WeatherService(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    public WeatherResponse getCurrentWeather(double lat, double lng) {
        return weatherClient.getWeather(lat, lng);
    }
}
