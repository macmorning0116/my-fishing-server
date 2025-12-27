package com.yechan.fishing.fishing_api.global.external.gpt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    private String baseUrl;
    private String apiKey;
    private String model;

    public String getBaseUrl() { return baseUrl; }
    public String getApiKey() { return apiKey; }
    public String getModel() { return model; }

    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setModel(String model) { this.model = model; }

}
