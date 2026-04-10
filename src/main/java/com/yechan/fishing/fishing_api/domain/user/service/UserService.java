package com.yechan.fishing.fishing_api.domain.user.service;

import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthUserResponse;
import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.jwt.IssuedTokens;
import com.yechan.fishing.fishing_api.domain.auth.jwt.JwtTokenProvider;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthSessionResult;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  public UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
    this.userRepository = userRepository;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public AuthUserResponse getMe(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));
    return new AuthUserResponse(
        user.getId(),
        user.getNickname(),
        user.getProfileImageUrl(),
        user.getRole(),
        user.getStatus());
  }

  public boolean isNicknameAvailable(String nickname) {
    return !userRepository.existsByNickname(nickname);
  }

  @Transactional
  public AuthSessionResult completeProfile(Long userId, String nickname, String userAgent) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));

    if (!user.isPending()) {
      throw new FishingException(ErrorCode.USER_PROFILE_ALREADY_SET);
    }

    if (userRepository.existsByNickname(nickname)) {
      throw new FishingException(ErrorCode.USER_NICKNAME_DUPLICATE);
    }

    LocalDateTime now = LocalDateTime.now();
    user.completeProfile(nickname, null, now);

    IssuedTokens tokens = jwtTokenProvider.issueTokens(user);

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
                user.getStatus())));
  }
}
