package com.yechan.fishing.fishing_api.domain.search.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record SearchPostsRequest(
    String q,
    String boardKey,
    String region,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate untilDate,
    String cursor,
    @Min(1) @Max(100) Integer size,
    String source) {
  public int safeSize() {
    return size == null ? 20 : size;
  }

  public String safeSource() {
    return source == null ? "all" : source;
  }
}
