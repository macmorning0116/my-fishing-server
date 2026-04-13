package com.yechan.fishing.fishing_api.domain.community.storage;

public record StoredCommunityImage(
    String imageUrl,
    String thumbnailUrl,
    Integer sortOrder,
    String contentType,
    Long fileSize,
    Integer width,
    Integer height) {}
