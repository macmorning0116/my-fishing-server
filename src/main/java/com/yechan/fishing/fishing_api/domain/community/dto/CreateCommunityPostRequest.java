package com.yechan.fishing.fishing_api.domain.community.dto;

import com.yechan.fishing.fishing_api.domain.community.entity.enums.FishedAtSource;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.LocationSource;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.TackleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateCommunityPostRequest(
    @NotBlank @Size(max = 5000) String content,
    @NotBlank @Size(max = 50) String region,
    @Size(max = 255) String placeName,
    @Min(-90) @Max(90) Double latitude,
    @Min(-180) @Max(180) Double longitude,
    LocalDateTime fishedAt,
    FishedAtSource fishedAtSource,
    LocationSource locationSource,
    @Size(max = 100) String species,
    @Min(0) Integer lengthCm,
    TackleType tackleType,
    @Size(max = 255) String tackleCustomText) {}
