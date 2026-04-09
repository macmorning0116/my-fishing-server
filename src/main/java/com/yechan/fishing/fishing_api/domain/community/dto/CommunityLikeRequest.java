package com.yechan.fishing.fishing_api.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityLikeRequest(@NotNull Long userId) {}
