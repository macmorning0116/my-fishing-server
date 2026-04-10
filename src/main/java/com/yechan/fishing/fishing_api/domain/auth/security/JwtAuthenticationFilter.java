package com.yechan.fishing.fishing_api.domain.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yechan.fishing.fishing_api.domain.auth.jwt.AccessTokenPayload;
import com.yechan.fishing.fishing_api.domain.auth.jwt.JwtTokenProvider;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String AUTHENTICATED_ACCESS_TOKEN_ATTRIBUTE =
      "authenticatedAccessTokenPayload";

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!authorizationHeader.startsWith("Bearer ")) {
      writeUnauthorized(response, ErrorCode.AUTH_INVALID_TOKEN);
      return;
    }

    String accessToken = authorizationHeader.substring(7).trim();
    if (accessToken.isBlank()) {
      writeUnauthorized(response, ErrorCode.AUTH_INVALID_TOKEN);
      return;
    }

    try {
      AccessTokenPayload payload = jwtTokenProvider.parseAccessToken(accessToken);
      request.setAttribute(AUTHENTICATED_ACCESS_TOKEN_ATTRIBUTE, payload);
      filterChain.doFilter(request, response);
    } catch (FishingException e) {
      writeUnauthorized(response, e.getErrorCode());
    }
  }

  private void writeUnauthorized(HttpServletResponse response, ErrorCode errorCode)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(
        response.getWriter(), ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
  }
}
