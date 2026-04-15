package com.yechan.fishing.fishing_api.global.external.opensearch;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opensearch")
public class OpenSearchProperties {

  private String baseUrl;
  private String indexName;
  private String communityIndexName;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getIndexName() {
    return indexName;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  public String getCommunityIndexName() {
    return communityIndexName;
  }

  public void setCommunityIndexName(String communityIndexName) {
    this.communityIndexName = communityIndexName;
  }
}
