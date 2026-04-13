package com.yechan.fishing.fishing_api.domain.community.dto;

import java.util.List;

public record CommunityCommentsResponse(
    List<CommunityCommentItem> items, int size, Long nextCursor) {}
