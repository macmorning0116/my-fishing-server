package com.yechan.fishing.fishing_api.domain.auth.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.kakao")
public class KakaoApiProperties {

  private String userInfoBaseUrl;

  public String getUserInfoBaseUrl() {
    return userInfoBaseUrl;
  }

  public void setUserInfoBaseUrl(String userInfoBaseUrl) {
    this.userInfoBaseUrl = userInfoBaseUrl;
  }
}
