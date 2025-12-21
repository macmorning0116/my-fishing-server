package com.yechan.fishing.fishing_api.global.config;

import com.yechan.fishing.fishing_api.global.external.naver.NaverApiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "naverWebClient")
    public WebClient webClient(NaverApiProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("x-ncp-apigw-api-key-id", props.getKeyId())
                .defaultHeader("x-ncp-apigw-api-key", props.getKey())
                .build();
    }
}
