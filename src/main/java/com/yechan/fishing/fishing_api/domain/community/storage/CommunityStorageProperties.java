package com.yechan.fishing.fishing_api.domain.community.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "community.storage")
public class CommunityStorageProperties {

  private String localRoot = "uploads";
  private String publicBasePath = "/uploads";

  public String getLocalRoot() {
    return localRoot;
  }

  public void setLocalRoot(String localRoot) {
    this.localRoot = localRoot;
  }

  public String getPublicBasePath() {
    return publicBasePath;
  }

  public void setPublicBasePath(String publicBasePath) {
    this.publicBasePath = publicBasePath;
  }
}
