package com.yechan.fishing.fishing_api.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommunityCommentRequest(
    @NotNull Long userId,
    Long parentCommentId,
    Long replyToUserId,
    @NotBlank @Size(max = 3000) String content) {}
