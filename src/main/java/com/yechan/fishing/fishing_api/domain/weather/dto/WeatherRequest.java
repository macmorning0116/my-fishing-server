package com.yechan.fishing.fishing_api.domain.weather.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WeatherRequest(

        @NotNull
        @Min(-90)
        @Max(90)
        Double lat,

        @NotNull
        @Min(-180)
        @Max(180)
        Double lng
) {
}
