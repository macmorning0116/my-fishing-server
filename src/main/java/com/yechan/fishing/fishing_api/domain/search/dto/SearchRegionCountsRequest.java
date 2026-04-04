package com.yechan.fishing.fishing_api.domain.search.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record SearchRegionCountsRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate untilDate
) {
}
