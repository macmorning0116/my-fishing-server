package com.yechan.fishing.fishing_api.domain.community.dto;

import com.yechan.fishing.fishing_api.domain.community.entity.enums.TackleType;
import java.time.LocalDateTime;

public record CommunityPostItem(
    Long id,
    Long authorId,
    String authorNickname,
    String authorProfileImageUrl,
    String content,
    String region,
    String placeName,
    Double latitude,
    Double longitude,
    LocalDateTime fishedAt,
    String species,
    Integer lengthCm,
    TackleType tackleType,
    String tackleCustomText,
    String thumbnailImageUrl,
    Integer likeCount,
    Integer commentCount,
    boolean likedByMe,
    LocalDateTime createdAt) {}
