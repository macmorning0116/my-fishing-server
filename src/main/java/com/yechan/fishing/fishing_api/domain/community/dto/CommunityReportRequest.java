package com.yechan.fishing.fishing_api.domain.community.dto;

import com.yechan.fishing.fishing_api.domain.community.entity.enums.ReportReasonType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommunityReportRequest(
    @NotNull ReportReasonType reasonType, @Size(max = 1000) String reasonDetail) {}
