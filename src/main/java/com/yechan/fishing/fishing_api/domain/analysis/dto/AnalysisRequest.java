package com.yechan.fishing.fishing_api.domain.analysis.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record AnalysisRequest(

        @NotNull(message = "이미지는 필수입니다.")
        MultipartFile image,

        @NotNull
        @Min(value = -90)
        @Max(value = 90)
        Double lat,

        @NotNull
        @Min(value = -180)
        @Max(value = 180)
        Double lng
        ) {
}
