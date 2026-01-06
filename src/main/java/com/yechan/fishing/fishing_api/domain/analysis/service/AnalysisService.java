package com.yechan.fishing.fishing_api.domain.analysis.service;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.global.external.gpt.GptClient;
import com.yechan.fishing.fishing_api.global.external.weather.WeatherClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class AnalysisService {

    private final WeatherClient weatherClient;
    private final GptClient gptClient;

    public AnalysisService(
            WeatherClient weatherClient,
            GptClient gptClient
    ) {
        this.weatherClient = weatherClient;
        this.gptClient = gptClient;
    }

    public AnalysisResponse analyze (
            MultipartFile image,
            double lat,
            double lng
    ) {
        GptWeatherContext weather = weatherClient.getGptWeatherContext(lat, lng);

        AnalysisResponse response = gptClient.analyze(image, weather);

        return response;
    }
}
