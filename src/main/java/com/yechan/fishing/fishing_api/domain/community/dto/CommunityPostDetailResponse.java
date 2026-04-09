package com.yechan.fishing.fishing_api.domain.community.dto;

import java.util.List;

public record CommunityPostDetailResponse(
    CommunityPostItem post, List<CommunityPostImageItem> images) {}
