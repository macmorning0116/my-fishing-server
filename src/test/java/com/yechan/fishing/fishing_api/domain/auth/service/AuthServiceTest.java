package com.yechan.fishing.fishing_api.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.yechan.fishing.fishing_api.domain.auth.client.SocialUserInfo;
import com.yechan.fishing.fishing_api.domain.auth.client.SocialUserInfoClient;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthorizationCodeLoginRequest;
import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.entity.UserRefreshToken;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserStatus;
import com.yechan.fishing.fishing_api.domain.auth.jwt.IssuedTokens;
import com.yechan.fishing.fishing_api.domain.auth.jwt.JwtTokenProvider;
import com.yechan.fishing.fishing_api.domain.auth.jwt.RefreshTokenPayload;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRefreshTokenRepository;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private UserRefreshTokenRepository userRefreshTokenRepository;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private SocialUserInfoClient kakaoClient;

  @Test
  void loginWithAuthorizationCode_whenNewUser_createsUserAndIssuesTokens() {
    given(kakaoClient.provider()).willReturn(AuthProvider.KAKAO);
    AuthService service =
        new AuthService(
            userRepository, userRefreshTokenRepository, jwtTokenProvider, List.of(kakaoClient));
    AuthorizationCodeLoginRequest request =
        new AuthorizationCodeLoginRequest("auth-code", "signed-state", "iPhone");
    SocialUserInfo socialUserInfo =
        new SocialUserInfo("provider-user-1", "angler@example.com", "앵글러", "https://image");
    IssuedTokens issuedTokens =
        new IssuedTokens(
            "access-token",
            "refresh-token",
            LocalDateTime.of(2026, 4, 10, 17, 0),
            LocalDateTime.of(2026, 4, 24, 17, 0));

    given(kakaoClient.exchangeCode("auth-code")).willReturn("provider-token");
    given(kakaoClient.getUserInfo("provider-token")).willReturn(socialUserInfo);
    org.mockito.BDDMockito.willDoNothing()
        .given(jwtTokenProvider)
        .validateOAuthState(AuthProvider.KAKAO, "signed-state");
    given(userRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "provider-user-1"))
        .willReturn(Optional.empty());
    given(userRepository.save(any(User.class)))
        .willAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              ReflectionTestUtils.setField(user, "id", 1L);
              return user;
            });
    given(jwtTokenProvider.issueTokens(any(User.class))).willReturn(issuedTokens);

    AuthSessionResult result =
        service.loginWithAuthorizationCode(AuthProvider.KAKAO, request, "ios-user-agent");

    assertEquals("access-token", result.response().accessToken());
    assertEquals("refresh-token", result.refreshToken());
    assertEquals(1L, result.response().user().id());
    assertEquals("앵글러", result.response().user().nickname());
    assertEquals(true, result.response().user().needsProfileSetup());
    then(userRefreshTokenRepository).should().save(any(UserRefreshToken.class));
  }

  @Test
  void getAuthorizationUrl_returnsSignedStateAndAuthorizationUrl() {
    given(kakaoClient.provider()).willReturn(AuthProvider.KAKAO);
    given(jwtTokenProvider.issueOAuthState(AuthProvider.KAKAO)).willReturn("signed-state");
    given(kakaoClient.buildAuthorizationUrl("signed-state"))
        .willReturn("https://provider.example.com/oauth/authorize?state=signed-state");

    AuthService service =
        new AuthService(
            userRepository, userRefreshTokenRepository, jwtTokenProvider, List.of(kakaoClient));

    var response = service.getAuthorizationUrl(AuthProvider.KAKAO);

    assertEquals("signed-state", response.state());
    assertEquals(
        "https://provider.example.com/oauth/authorize?state=signed-state",
        response.authorizationUrl());
  }

  @Test
  void refresh_whenSavedTokenMissing_throwsRefreshTokenNotFound() {
    given(kakaoClient.provider()).willReturn(AuthProvider.KAKAO);
    AuthService service =
        new AuthService(
            userRepository, userRefreshTokenRepository, jwtTokenProvider, List.of(kakaoClient));

    given(userRefreshTokenRepository.findByRefreshTokenAndRevokedAtIsNull("missing-token"))
        .willReturn(Optional.empty());

    FishingException exception =
        assertThrows(
            FishingException.class, () -> service.refresh("missing-token", "ios-user-agent"));

    assertEquals(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void refresh_whenTokenIsValid_issuesNewTokens() {
    given(kakaoClient.provider()).willReturn(AuthProvider.KAKAO);
    AuthService service =
        new AuthService(
            userRepository, userRefreshTokenRepository, jwtTokenProvider, List.of(kakaoClient));
    User user =
        User.create(
            AuthProvider.GOOGLE,
            "google-user-1",
            "angler@example.com",
            "앵글러",
            null,
            LocalDateTime.now().minusDays(1));
    ReflectionTestUtils.setField(user, "id", 2L);
    UserRefreshToken savedToken =
        UserRefreshToken.issue(
            user,
            "old-refresh-token",
            LocalDateTime.now().plusDays(7),
            "old-device",
            "old-agent",
            LocalDateTime.now().minusDays(1));
    IssuedTokens issuedTokens =
        new IssuedTokens(
            "new-access-token",
            "new-refresh-token",
            LocalDateTime.of(2026, 4, 10, 17, 0),
            LocalDateTime.of(2026, 4, 24, 17, 0));

    given(userRefreshTokenRepository.findByRefreshTokenAndRevokedAtIsNull("old-refresh-token"))
        .willReturn(Optional.of(savedToken));
    given(jwtTokenProvider.parseRefreshToken("old-refresh-token"))
        .willReturn(new RefreshTokenPayload(2L, LocalDateTime.now().plusDays(7)));
    given(jwtTokenProvider.issueTokens(user)).willReturn(issuedTokens);
    given(userRefreshTokenRepository.save(any(UserRefreshToken.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    AuthSessionResult result = service.refresh("old-refresh-token", "ios-user-agent");

    assertEquals("new-access-token", result.response().accessToken());
    assertEquals("new-refresh-token", result.refreshToken());
    assertNotNull(savedToken.getRevokedAt());
    assertEquals(UserStatus.PENDING, result.response().user().status());
    assertEquals(true, result.response().user().needsProfileSetup());
  }

  @Test
  void logout_whenSavedTokenExists_revokesToken() {
    given(kakaoClient.provider()).willReturn(AuthProvider.KAKAO);
    AuthService service =
        new AuthService(
            userRepository, userRefreshTokenRepository, jwtTokenProvider, List.of(kakaoClient));
    User user =
        User.create(
            AuthProvider.KAKAO,
            "kakao-user-1",
            "angler@example.com",
            "앵글러",
            null,
            LocalDateTime.now().minusDays(1));
    UserRefreshToken savedToken =
        UserRefreshToken.issue(
            user,
            "refresh-token",
            LocalDateTime.now().plusDays(7),
            "iphone",
            "ios-agent",
            LocalDateTime.now().minusHours(1));

    given(userRefreshTokenRepository.findByRefreshTokenAndRevokedAtIsNull("refresh-token"))
        .willReturn(Optional.of(savedToken));

    service.logout("refresh-token");

    assertNotNull(savedToken.getRevokedAt());
  }
}
