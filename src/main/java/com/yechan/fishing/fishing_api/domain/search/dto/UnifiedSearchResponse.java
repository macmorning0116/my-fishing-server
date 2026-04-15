package com.yechan.fishing.fishing_api.domain.search.dto;

import java.util.List;

public record UnifiedSearchResponse(
    List<SearchResultItem> items, long total, int size, String nextCursor) {}
