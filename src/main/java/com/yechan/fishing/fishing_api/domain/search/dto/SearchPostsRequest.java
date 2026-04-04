package com.yechan.fishing.fishing_api.domain.search.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record SearchPostsRequest(
        String q,
        String boardKey,
        String region,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate untilDate,
        String cursor,
        @Min(1) @Max(100) Integer size
) {
    public int safeSize() {
        return size == null ? 20 : size;
    }
}
