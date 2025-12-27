package com.yechan.fishing.fishing_api.domain.analysis.dto;

public record AnalysisPoint(
        double x,               // 0~1 (좌상단 기준 비율)
        double y,               // 0~1
        double radius,          // 0~1 (이미지 너비 기준)
        String reason
) {
}
