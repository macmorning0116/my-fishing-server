package com.yechan.fishing.fishing_api.domain.community.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CommunityPostsRequest(Long cursor, @Min(1) @Max(50) Integer size, Long authorId) {

  public int safeSize() {
    return size == null ? 20 : size;
  }
}
