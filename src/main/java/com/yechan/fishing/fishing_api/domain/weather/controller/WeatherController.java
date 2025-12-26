package com.yechan.fishing.fishing_api.domain.weather.controller;

import com.yechan.fishing.fishing_api.domain.weather.dto.WeatherRequest;
import com.yechan.fishing.fishing_api.domain.weather.dto.WeatherResponse;
import com.yechan.fishing.fishing_api.domain.weather.service.WeatherService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public ApiResponse<WeatherResponse> getCurrentWeather(
            @Valid WeatherRequest request
            ) {
        return ApiResponse.success(weatherService.getCurrentWeather(request.lat(), request.lng()));
    }
}
