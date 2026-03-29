package com.yechan.fishing.fishing_api.domain.analysis.controller;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisPoint;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisService analysisService;

    @Test
    void analyze_returnsWrappedSuccessResponse() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "spot.jpg",
                "image/jpeg",
                "image".getBytes()
        );

        AnalysisResponse response = new AnalysisResponse(
                "요약",
                List.of(
                        new AnalysisPoint(0.2, 0.3, 0.1, "첫 번째 이유"),
                        new AnalysisPoint(0.6, 0.7, 0.12, "두 번째 이유")
                ),
                "채비",
                "전략"
        );

        given(analysisService.analyze(any(), eq(37.5), eq(127.0))).willReturn(response);

        mockMvc.perform(multipart("/v1/analysis/photo")
                        .file(image)
                        .param("lat", "37.5")
                        .param("lng", "127.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").value("요약"))
                .andExpect(jsonPath("$.data.points.length()").value(2))
                .andExpect(jsonPath("$.data.tackle").value("채비"))
                .andExpect(jsonPath("$.data.strategy").value("전략"));
    }

    @Test
    void analyze_whenImageIsMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/v1/analysis/photo")
                        .param("lat", "37.5")
                        .param("lng", "127.0"))
                .andExpect(status().isBadRequest());
    }
}
