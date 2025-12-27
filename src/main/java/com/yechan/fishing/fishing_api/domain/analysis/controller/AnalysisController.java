package com.yechan.fishing.fishing_api.domain.analysis.controller;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisRequest;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.service.AnalysisService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/photo")
    public ApiResponse<AnalysisResponse> analyze(
            @Valid @ModelAttribute AnalysisRequest request
            ) {
        return ApiResponse.success(
                analysisService.analyze(
                        request.image(),
                        request.lat(),
                        request.lng()
                )
        );
    }
}
