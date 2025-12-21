package com.yechan.fishing.fishing_api.domain.map.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReverseGeocodeRequest(

        @NotNull(message = "위도는 필수입니다.")
        @Min(value = -90, message = "위도는 -90 이상이어야 합니다.")
        @Max(value = 90, message = "위도는 90 이하여야 합니다.")
        Double lat,

        @NotNull(message = "경도는 필수입니다.")
        @Min(value = -180, message = "위도는 -180 이상이어야 합니다.")
        @Max(value = 180, message = "위도는 180 이하여야 합니다.")
        Double lng
) {
}
