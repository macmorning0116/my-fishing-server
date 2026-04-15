package com.yechan.fishing.fishing_api.domain.search.dto;

import java.util.List;

public record SearchResultItem(
    String type,
    String articleId,
    String title,
    String url,
    String boardName,
    Long postId,
    String thumbnailImageUrl,
    String authorProfileImageUrl,
    String tackleType,
    String authorName,
    String publishedAt,
    String species,
    String region,
    String place,
    String contentPreview,
    List<String> tags) {}
