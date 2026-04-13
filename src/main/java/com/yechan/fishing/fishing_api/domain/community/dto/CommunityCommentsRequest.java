package com.yechan.fishing.fishing_api.domain.community.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CommunityCommentsRequest(Long cursor, @Min(1) @Max(100) Integer size) {

  public int safeSize() {
    return size == null ? 50 : size;
  }
}
