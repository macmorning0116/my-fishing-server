package com.yechan.fishing.fishing_api.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommunityCommentRequest(@NotBlank @Size(max = 3000) String content) {}
