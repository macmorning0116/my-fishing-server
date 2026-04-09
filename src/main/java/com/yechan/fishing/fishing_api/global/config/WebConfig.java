package com.yechan.fishing.fishing_api.global.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final String localRoot;
  private final String publicBasePath;

  public WebConfig(
      @Value("${community.storage.local-root:${user.dir}/uploads}") String localRoot,
      @Value("${community.storage.public-base-path:/uploads}") String publicBasePath) {
    this.localRoot = localRoot;
    this.publicBasePath = publicBasePath;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String normalizedPublicBasePath = normalizePublicBasePath(publicBasePath);
    String resourceLocation = Path.of(localRoot).toAbsolutePath().normalize().toUri().toString();

    registry
        .addResourceHandler(normalizedPublicBasePath + "/**")
        .addResourceLocations(resourceLocation);
  }

  private String normalizePublicBasePath(String publicBasePath) {
    if (publicBasePath == null || publicBasePath.isBlank()) {
      return "/uploads";
    }

    String normalized = publicBasePath.startsWith("/") ? publicBasePath : "/" + publicBasePath;
    return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
  }
}
