package com.yechan.fishing.fishing_api.global.config;

import com.yechan.fishing.fishing_api.global.external.gpt.OpenAiProperties;
import com.yechan.fishing.fishing_api.global.external.naver.NaverApiProperties;
import com.yechan.fishing.fishing_api.global.external.weather.OpenWeatherApiProperties;
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
    public WebClient naverWebClient(NaverApiProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("x-ncp-apigw-api-key-id", props.getKeyId())
                .defaultHeader("x-ncp-apigw-api-key", props.getKey())
                .build();
    }

    @Bean(name = "openWeatherWebClient")
    public WebClient openWeatherWebClient(OpenWeatherApiProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "openAiWebClient")
    public WebClient openAiWebClient(OpenAiProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }


}
