package com.yechan.fishing.fishing_api.domain.analysis.dto;

public record GptWeatherContext(
        double lat,
        double lng,
        long timestamp, // dt
        double temperature, // temp
        double feelsLike,
        int humidity,
        double windSpeed,
        int windDeg,
        int cloudiness,
        String weatherMain, // Clouds, Clear
        String weatherDesc,
        long sunrise,
        long sunset
) {
}
