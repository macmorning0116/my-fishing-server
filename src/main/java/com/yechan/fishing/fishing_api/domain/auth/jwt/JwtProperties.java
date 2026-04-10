package com.yechan.fishing.fishing_api.domain.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

  private String issuer = "fishing-api";
  private String secret = "dev-secret-key-change-me-dev-secret-key-change-me";
  private long accessTokenExpirationSeconds = 1800;
  private long refreshTokenExpirationSeconds = 1209600;

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getAccessTokenExpirationSeconds() {
    return accessTokenExpirationSeconds;
  }

  public void setAccessTokenExpirationSeconds(long accessTokenExpirationSeconds) {
    this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
  }

  public long getRefreshTokenExpirationSeconds() {
    return refreshTokenExpirationSeconds;
  }

  public void setRefreshTokenExpirationSeconds(long refreshTokenExpirationSeconds) {
    this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
  }
}
