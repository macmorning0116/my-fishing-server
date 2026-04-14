package com.yechan.fishing.fishing_api.domain.community.dto;

public record MapPostItem(
    Long id,
    String thumbnailImageUrl,
    String species,
    String contentPreview,
    Double latitude,
    Double longitude) {}
