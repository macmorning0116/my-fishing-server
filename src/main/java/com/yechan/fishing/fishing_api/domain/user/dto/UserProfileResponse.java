package com.yechan.fishing.fishing_api.domain.user.dto;

public record UserProfileResponse(
    Long id, String nickname, String profileImageUrl, long postCount) {}
