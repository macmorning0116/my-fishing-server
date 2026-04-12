package com.yechan.fishing.fishing_api.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yechan.fishing.fishing_api.domain.auth.jwt.JwtTokenProvider;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.domain.auth.security.CurrentUserArgumentResolver;
import com.yechan.fishing.fishing_api.domain.auth.security.JwtAuthenticationFilter;
import java.nio.file.Path;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final String localRoot;
  private final String publicBasePath;
  private final ObjectProvider<CurrentUserArgumentResolver> currentUserArgumentResolverProvider;
  private final ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider;

  public WebConfig(
      @Value("${community.storage.local-root:${user.dir}/uploads}") String localRoot,
      @Value("${community.storage.public-base-path:/uploads}") String publicBasePath,
      ObjectProvider<CurrentUserArgumentResolver> currentUserArgumentResolverProvider,
      ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider) {
    this.localRoot = localRoot;
    this.publicBasePath = publicBasePath;
    this.currentUserArgumentResolverProvider = currentUserArgumentResolverProvider;
    this.jwtAuthenticationFilterProvider = jwtAuthenticationFilterProvider;
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

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    currentUserArgumentResolverProvider.ifAvailable(resolvers::add);
  }

  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration() {
    FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>();
    JwtAuthenticationFilter filter = jwtAuthenticationFilterProvider.getIfAvailable();
    if (filter != null) {
      bean.setFilter(filter);
      bean.addUrlPatterns("/*");
      bean.setOrder(2);
    } else {
      bean.setEnabled(false);
    }
    return bean;
  }

  @Bean
  public CurrentUserArgumentResolver currentUserArgumentResolver(UserRepository userRepository) {
    return new CurrentUserArgumentResolver(userRepository);
  }

  @Bean
  @ConditionalOnBean(JwtTokenProvider.class)
  public JwtAuthenticationFilter jwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
    return new JwtAuthenticationFilter(jwtTokenProvider, objectMapper);
  }

  private String normalizePublicBasePath(String publicBasePath) {
    if (publicBasePath == null || publicBasePath.isBlank()) {
      return "/uploads";
    }

    String normalized = publicBasePath.startsWith("/") ? publicBasePath : "/" + publicBasePath;
    return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
  }
}
