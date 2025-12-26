package com.yechan.fishing.fishing_api.domain.weather.dto;

public record WeatherResponse(
        double temperature,
        String weather,
        long sunrise,
        long sunset
) {
}
