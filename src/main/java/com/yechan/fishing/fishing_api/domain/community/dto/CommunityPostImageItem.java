package com.yechan.fishing.fishing_api.domain.community.dto;

public record CommunityPostImageItem(
    Long id,
    String imageUrl,
    Integer sortOrder,
    String contentType,
    Long fileSize,
    Integer width,
    Integer height) {}
