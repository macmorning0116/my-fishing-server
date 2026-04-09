package com.yechan.fishing.fishing_api.domain.community.dto;

import com.yechan.fishing.fishing_api.domain.community.entity.enums.ReportStatus;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;

public record CommunityReportResponse(
    Long reportId, ReportStatus status, VisibilityStatus targetVisibilityStatus) {}
