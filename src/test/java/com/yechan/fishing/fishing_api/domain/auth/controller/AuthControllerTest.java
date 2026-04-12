package com.yechan.fishing.fishing_api.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yechan.fishing.fishing_api.domain.auth.cookie.RefreshTokenCookieManager;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthUserResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthorizationCodeLoginRequest;
import com.yechan.fishing.fishing_api.domain.auth.dto.SocialAuthorizationUrlResponse;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserRole;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserStatus;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthService;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthSessionResult;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthService authService;

  @MockBean private RefreshTokenCookieManager refreshTokenCookieManager;

  @Test
  void loginWithAuthorizationCode_returnsWrappedSuccessResponse() throws Exception {
    AuthorizationCodeLoginRequest request =
        new AuthorizationCodeLoginRequest("auth-code", "signed-state", "iPhone");
    AuthSessionResult result =
        new AuthSessionResult(
            "refresh-token",
            new AuthTokenResponse(
                "access-token",
                LocalDateTime.of(2026, 4, 10, 17, 0),
                LocalDateTime.of(2026, 4, 24, 17, 0),
                new AuthUserResponse(
                    1L,
                    "앵글러",
                    "https://image",
                    UserRole.USER,
                    UserStatus.ACTIVE,
                    false,
                    "test@test.com",
                    AuthProvider.KAKAO,
                    null)));

    given(authService.loginWithAuthorizationCode(any(), any(), any())).willReturn(result);

    mockMvc
        .perform(
            post("/v1/auth/kakao/code")
                .header("User-Agent", "ios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accessToken").value("access-token"))
        .andExpect(jsonPath("$.data.user.nickname").value("앵글러"));
  }

  @Test
  void getAuthorizationUrl_returnsGetResponse() throws Exception {
    SocialAuthorizationUrlResponse response =
        new SocialAuthorizationUrlResponse(
            "https://provider.example.com/oauth/authorize?state=signed-state", "signed-state");

    given(authService.getAuthorizationUrl(AuthProvider.GOOGLE)).willReturn(response);

    mockMvc
        .perform(get("/v1/auth/google/authorize-url"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(
            jsonPath("$.data.authorizationUrl")
                .value("https://provider.example.com/oauth/authorize?state=signed-state"))
        .andExpect(jsonPath("$.data.state").value("signed-state"));
  }

  @Test
  void refresh_returnsWrappedSuccessResponse() throws Exception {
    AuthSessionResult result =
        new AuthSessionResult(
            "new-refresh-token",
            new AuthTokenResponse(
                "new-access-token",
                LocalDateTime.of(2026, 4, 10, 17, 0),
                LocalDateTime.of(2026, 4, 24, 17, 0),
                new AuthUserResponse(
                    1L,
                    "앵글러",
                    "https://image",
                    UserRole.USER,
                    UserStatus.ACTIVE,
                    false,
                    "test@test.com",
                    AuthProvider.KAKAO,
                    null)));

    given(refreshTokenCookieManager.extractRefreshToken(any())).willReturn("refresh-token");
    given(authService.refresh("refresh-token", "ios")).willReturn(result);

    mockMvc
        .perform(
            post("/v1/auth/refresh")
                .header("User-Agent", "ios")
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refresh-token")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
  }

  @Test
  void logout_clearsRefreshTokenCookie() throws Exception {
    given(refreshTokenCookieManager.extractRefreshToken(any())).willReturn("refresh-token");

    mockMvc
        .perform(
            post("/v1/auth/logout")
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refresh-token")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").doesNotExist());
  }
}
