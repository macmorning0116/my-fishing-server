package com.yechan.fishing.fishing_api.domain.auth.service;

import com.yechan.fishing.fishing_api.domain.auth.client.SocialUserInfo;
import com.yechan.fishing.fishing_api.domain.auth.client.SocialUserInfoClient;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthUserResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthorizationCodeLoginRequest;
import com.yechan.fishing.fishing_api.domain.auth.dto.SocialAuthorizationUrlResponse;
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
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final UserRefreshTokenRepository userRefreshTokenRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final Map<AuthProvider, SocialUserInfoClient> socialUserInfoClients;

  public AuthService(
      UserRepository userRepository,
      UserRefreshTokenRepository userRefreshTokenRepository,
      JwtTokenProvider jwtTokenProvider,
      List<SocialUserInfoClient> socialUserInfoClients) {
    this.userRepository = userRepository;
    this.userRefreshTokenRepository = userRefreshTokenRepository;
    this.jwtTokenProvider = jwtTokenProvider;
    this.socialUserInfoClients =
        socialUserInfoClients.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    SocialUserInfoClient::provider, Function.identity()));
  }

  public SocialAuthorizationUrlResponse getAuthorizationUrl(AuthProvider provider) {
    SocialUserInfoClient client = getClient(provider);
    String state = jwtTokenProvider.issueOAuthState(provider);
    return new SocialAuthorizationUrlResponse(client.buildAuthorizationUrl(state), state);
  }

  @Transactional
  public AuthSessionResult loginWithAuthorizationCode(
      AuthProvider provider, AuthorizationCodeLoginRequest request, String userAgent) {
    jwtTokenProvider.validateOAuthState(provider, request.state());

    SocialUserInfoClient client = getClient(provider);
    String providerAccessToken = client.exchangeCode(request.code());
    SocialUserInfo socialUserInfo = client.getUserInfo(providerAccessToken);
    LocalDateTime now = LocalDateTime.now();

    User user =
        userRepository
            .findByProviderAndProviderUserId(provider, socialUserInfo.providerUserId())
            .map(
                existingUser -> {
                  existingUser.updateSocialProfile(
                      socialUserInfo.email(), socialUserInfo.profileImageUrl(), now);
                  return existingUser;
                })
            .orElseGet(
                () ->
                    User.create(
                        provider,
                        socialUserInfo.providerUserId(),
                        socialUserInfo.email(),
                        resolveNickname(provider, socialUserInfo),
                        socialUserInfo.profileImageUrl(),
                        now));

    user = userRepository.save(user);
    if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.DELETED) {
      throw new FishingException(ErrorCode.AUTH_USER_INACTIVE);
    }

    IssuedTokens tokens = jwtTokenProvider.issueTokens(user);
    userRefreshTokenRepository.save(
        UserRefreshToken.issue(
            user,
            tokens.refreshToken(),
            tokens.refreshTokenExpiresAt(),
            request.deviceName(),
            userAgent,
            now));

    return toAuthSessionResult(user, tokens);
  }

  @Transactional
  public AuthSessionResult refresh(String refreshToken, String userAgent) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new FishingException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND);
    }

    LocalDateTime now = LocalDateTime.now();
    UserRefreshToken savedToken =
        userRefreshTokenRepository
            .findByRefreshTokenAndRevokedAtIsNull(refreshToken)
            .orElseThrow(() -> new FishingException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND));

    RefreshTokenPayload payload = jwtTokenProvider.parseRefreshToken(refreshToken);
    if (!savedToken.getUser().getId().equals(payload.userId())
        || savedToken.isRevoked()
        || savedToken.isExpired(now)
        || payload.expiresAt().isBefore(now)) {
      throw new FishingException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }

    User user = savedToken.getUser();
    if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.DELETED) {
      throw new FishingException(ErrorCode.AUTH_USER_INACTIVE);
    }
    savedToken.revoke(now);

    IssuedTokens tokens = jwtTokenProvider.issueTokens(user);
    userRefreshTokenRepository.save(
        UserRefreshToken.issue(
            user,
            tokens.refreshToken(),
            tokens.refreshTokenExpiresAt(),
            savedToken.getDeviceName(),
            userAgent,
            now));

    return toAuthSessionResult(user, tokens);
  }

  @Transactional
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }

    userRefreshTokenRepository
        .findByRefreshTokenAndRevokedAtIsNull(refreshToken)
        .ifPresent(token -> token.revoke(LocalDateTime.now()));
  }

  private SocialUserInfoClient getClient(AuthProvider provider) {
    SocialUserInfoClient client = socialUserInfoClients.get(provider);
    if (client == null) {
      throw new FishingException(ErrorCode.AUTH_PROVIDER_NOT_SUPPORTED);
    }
    return client;
  }

  private String resolveNickname(AuthProvider provider, SocialUserInfo socialUserInfo) {
    if (socialUserInfo.nickname() != null && !socialUserInfo.nickname().isBlank()) {
      return socialUserInfo.nickname();
    }
    if (socialUserInfo.email() != null && socialUserInfo.email().contains("@")) {
      return socialUserInfo.email().substring(0, socialUserInfo.email().indexOf('@'));
    }

    String providerUserId = socialUserInfo.providerUserId();
    String suffix =
        providerUserId == null
            ? "user"
            : providerUserId.substring(Math.max(0, providerUserId.length() - 6));
    return provider.name().toLowerCase() + "_" + suffix;
  }

  private AuthSessionResult toAuthSessionResult(User user, IssuedTokens tokens) {
    return new AuthSessionResult(
        tokens.refreshToken(),
        new AuthTokenResponse(
            tokens.accessToken(),
            tokens.accessTokenExpiresAt(),
            tokens.refreshTokenExpiresAt(),
            new AuthUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole(),
                user.getStatus(),
                user.needsProfileSetup(),
                user.getEmail(),
                user.getProvider(),
                user.getNicknameChangedAt())));
  }
}
