package com.yechan.fishing.fishing_api.domain.analysis.dto;

import java.util.List;

public record AnalysisResponse(
        String summary,
        List<AnalysisPoint> points,
        String tackle,
        String strategy
) {
}
