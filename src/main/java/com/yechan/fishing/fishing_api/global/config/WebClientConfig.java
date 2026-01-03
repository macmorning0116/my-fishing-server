package com.yechan.fishing.fishing_api.global.config;

import com.yechan.fishing.fishing_api.global.external.gpt.OpenAiProperties;
import com.yechan.fishing.fishing_api.global.external.naver.NaverApiProperties;
import com.yechan.fishing.fishing_api.global.external.weather.OpenWeatherApiProperties;
import com.yechan.fishing.fishing_api.global.logging.WebClientLoggingFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties({
        NaverApiProperties.class,
        OpenWeatherApiProperties.class,
        OpenAiProperties.class
})
public class WebClientConfig {

    @Bean(name = "naverWebClient")
    public WebClient naverWebClient(NaverApiProperties props, WebClientLoggingFilter webClientLoggingFilter) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("x-ncp-apigw-api-key-id", props.getKeyId())
                .defaultHeader("x-ncp-apigw-api-key", props.getKey())
                .filter(webClientLoggingFilter.externalApiLoggingFilter("naver"))
                .build();
    }

    @Bean(name = "openWeatherWebClient")
    public WebClient openWeatherWebClient(OpenWeatherApiProperties props, WebClientLoggingFilter webClientLoggingFilter) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .filter(webClientLoggingFilter.externalApiLoggingFilter("openWeather"))
                .build();
    }

    @Bean(name = "openAiWebClient")
    public WebClient openAiWebClient(OpenAiProperties props, WebClientLoggingFilter webClientLoggingFilter) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .filter(webClientLoggingFilter.externalApiLoggingFilter("openAi"))
                .build();
    }


}
