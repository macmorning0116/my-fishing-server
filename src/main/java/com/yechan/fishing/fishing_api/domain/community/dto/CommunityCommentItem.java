package com.yechan.fishing.fishing_api.domain.community.dto;

import java.time.LocalDateTime;

public record CommunityCommentItem(
    Long id,
    Long userId,
    String userNickname,
    String userProfileImageUrl,
    Long parentCommentId,
    Long replyToUserId,
    String replyToNickname,
    String content,
    Integer likeCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean deleted) {}
