package com.yechan.fishing.fishing_api.global.config;

import com.yechan.fishing.fishing_api.domain.auth.client.GoogleApiProperties;
import com.yechan.fishing.fishing_api.domain.auth.client.KakaoApiProperties;
import com.yechan.fishing.fishing_api.global.external.gpt.OpenAiProperties;
import com.yechan.fishing.fishing_api.global.external.naver.NaverApiProperties;
import com.yechan.fishing.fishing_api.global.external.opensearch.OpenSearchProperties;
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
  OpenAiProperties.class,
  OpenSearchProperties.class,
  KakaoApiProperties.class,
  GoogleApiProperties.class
})
public class WebClientConfig {

  @Bean(name = "naverWebClient")
  public WebClient naverWebClient(
      NaverApiProperties props, WebClientLoggingFilter webClientLoggingFilter) {
    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .defaultHeader("x-ncp-apigw-api-key-id", props.getKeyId())
        .defaultHeader("x-ncp-apigw-api-key", props.getKey())
        .filter(webClientLoggingFilter.externalApiLoggingFilter("naver"))
        .build();
  }

  @Bean(name = "openWeatherWebClient")
  public WebClient openWeatherWebClient(
      OpenWeatherApiProperties props, WebClientLoggingFilter webClientLoggingFilter) {
    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .defaultHeader("Content-Type", "application/json")
        .filter(webClientLoggingFilter.externalApiLoggingFilter("openWeather"))
        .build();
  }

  @Bean(name = "openAiWebClient")
  public WebClient openAiWebClient(
      OpenAiProperties props, WebClientLoggingFilter webClientLoggingFilter) {
    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .defaultHeader("Authorization", "Bearer " + props.getApiKey())
        .defaultHeader("Content-Type", "application/json")
        .filter(webClientLoggingFilter.externalApiLoggingFilter("openAi"))
        .build();
  }

  @Bean(name = "openSearchWebClient")
  public WebClient openSearchWebClient(
      OpenSearchProperties props, WebClientLoggingFilter webClientLoggingFilter) {
    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .defaultHeader("Content-Type", "application/json")
        .filter(webClientLoggingFilter.externalApiLoggingFilter("openSearch"))
        .build();
  }

  @Bean(name = "kakaoAuthWebClient")
  public WebClient kakaoAuthWebClient(
      KakaoApiProperties props, WebClientLoggingFilter webClientLoggingFilter) {
    return WebClient.builder()
        .baseUrl(props.getUserInfoBaseUrl())
        .filter(webClientLoggingFilter.externalApiLoggingFilter("kakaoAuth"))
        .build();
  }

  @Bean(name = "googleAuthWebClient")
  public WebClient googleAuthWebClient(
      GoogleApiProperties props, WebClientLoggingFilter webClientLoggingFilter) {
    return WebClient.builder()
        .baseUrl(props.getUserInfoBaseUrl())
        .filter(webClientLoggingFilter.externalApiLoggingFilter("googleAuth"))
        .build();
  }
}
