package com.yechan.fishing.fishing_api.domain.analysis.service;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisPoint;
import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.global.external.gpt.GptClient;
import com.yechan.fishing.fishing_api.global.external.weather.WeatherClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private WeatherClient weatherClient;

    @Mock
    private GptClient gptClient;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void analyze_fetchesWeatherThenDelegatesToGpt() {
        MockMultipartFile image = new MockMultipartFile("image", "a.jpg", "image/jpeg", "img".getBytes());
        GptWeatherContext weather = new GptWeatherContext(37.5, 127.0, 100L, 20.0, 19.0, 50, 2.0, 90, 20, "Clear", "clear sky", 10L, 90L);
        AnalysisResponse response = new AnalysisResponse(
                "요약",
                List.of(
                        new AnalysisPoint(0.2, 0.3, 0.1, "이유1"),
                        new AnalysisPoint(0.6, 0.7, 0.1, "이유2")
                ),
                "채비",
                "전략"
        );

        given(weatherClient.getGptWeatherContext(37.5, 127.0)).willReturn(weather);
        given(gptClient.analyze(image, weather)).willReturn(response);

        AnalysisResponse result = analysisService.analyze(image, 37.5, 127.0);

        assertSame(response, result);
        then(weatherClient).should().getGptWeatherContext(37.5, 127.0);
        then(gptClient).should().analyze(image, weather);
    }
}
