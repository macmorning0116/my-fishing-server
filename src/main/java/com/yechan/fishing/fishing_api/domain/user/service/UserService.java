package com.yechan.fishing.fishing_api.domain.user.service;

import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthUserResponse;
import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.jwt.IssuedTokens;
import com.yechan.fishing.fishing_api.domain.auth.jwt.JwtTokenProvider;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthSessionResult;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import com.yechan.fishing.fishing_api.domain.community.repository.CommunityPostRepository;
import com.yechan.fishing.fishing_api.domain.community.storage.ImageStorageService;
import com.yechan.fishing.fishing_api.domain.user.dto.UserProfileResponse;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final ImageStorageService imageStorageService;
  private final CommunityPostRepository communityPostRepository;

  public UserService(
      UserRepository userRepository,
      JwtTokenProvider jwtTokenProvider,
      ImageStorageService imageStorageService,
      CommunityPostRepository communityPostRepository) {
    this.userRepository = userRepository;
    this.jwtTokenProvider = jwtTokenProvider;
    this.imageStorageService = imageStorageService;
    this.communityPostRepository = communityPostRepository;
  }

  public AuthUserResponse getMe(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));
    return toAuthUserResponse(user);
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
            toAuthUserResponse(user)));
  }

  @Transactional
  public AuthUserResponse updateNickname(Long userId, String nickname) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));
    user.ensureActive();
    if (userRepository.existsByNickname(nickname)) {
      throw new FishingException(ErrorCode.USER_NICKNAME_DUPLICATE);
    }
    user.updateNickname(nickname, LocalDateTime.now());
    return toAuthUserResponse(user);
  }

  @Transactional
  public AuthUserResponse updateProfileImage(Long userId, MultipartFile file) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));
    user.ensureActive();
    try {
      String imageUrl = imageStorageService.storeProfileImage(file);
      user.updateProfileImage(imageUrl, LocalDateTime.now());
    } catch (Exception e) {
      throw new FishingException(ErrorCode.USER_PROFILE_IMAGE_UPLOAD_ERROR);
    }
    return toAuthUserResponse(user);
  }

  @Transactional
  public AuthUserResponse resetProfileImage(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));
    user.ensureActive();
    user.updateProfileImage(null, LocalDateTime.now());
    return toAuthUserResponse(user);
  }

  @Transactional(readOnly = true)
  public UserProfileResponse getUserProfile(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));
    long postCount =
        communityPostRepository.countByUser_IdAndVisibilityStatus(userId, VisibilityStatus.VISIBLE);
    return new UserProfileResponse(
        user.getId(), user.getNickname(), user.getProfileImageUrl(), postCount);
  }

  private AuthUserResponse toAuthUserResponse(User user) {
    return new AuthUserResponse(
        user.getId(),
        user.getNickname(),
        user.getProfileImageUrl(),
        user.getRole(),
        user.getStatus(),
        user.needsProfileSetup(),
        user.getEmail(),
        user.getProvider(),
        user.getNicknameChangedAt());
  }
}
