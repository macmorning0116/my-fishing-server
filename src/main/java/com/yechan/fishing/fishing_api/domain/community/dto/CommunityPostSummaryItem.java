package com.yechan.fishing.fishing_api.domain.community.dto;

import java.time.LocalDateTime;

public record CommunityPostSummaryItem(
    Long id,
    Long authorId,
    String authorNickname,
    String authorProfileImageUrl,
    String contentPreview,
    String region,
    String species,
    String thumbnailImageUrl,
    Integer likeCount,
    Integer commentCount,
    boolean likedByMe,
    LocalDateTime createdAt) {}
