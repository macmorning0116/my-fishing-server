package com.yechan.fishing.fishing_api.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CommunityPostImageRequest(
    @NotBlank String imageUrl,
    @NotNull @PositiveOrZero Integer sortOrder,
    String contentType,
    @PositiveOrZero Long fileSize,
    @PositiveOrZero Integer width,
    @PositiveOrZero Integer height) {}
