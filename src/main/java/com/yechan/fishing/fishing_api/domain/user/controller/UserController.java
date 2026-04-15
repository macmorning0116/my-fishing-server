package com.yechan.fishing.fishing_api.domain.user.controller;

import com.yechan.fishing.fishing_api.domain.auth.cookie.RefreshTokenCookieManager;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthTokenResponse;
import com.yechan.fishing.fishing_api.domain.auth.dto.AuthUserResponse;
import com.yechan.fishing.fishing_api.domain.auth.security.AuthenticatedUser;
import com.yechan.fishing.fishing_api.domain.auth.security.CurrentUser;
import com.yechan.fishing.fishing_api.domain.auth.service.AuthSessionResult;
import com.yechan.fishing.fishing_api.domain.user.dto.CompleteProfileRequest;
import com.yechan.fishing.fishing_api.domain.user.dto.NicknameCheckResponse;
import com.yechan.fishing.fishing_api.domain.user.dto.UpdateNicknameRequest;
import com.yechan.fishing.fishing_api.domain.user.dto.UserProfileResponse;
import com.yechan.fishing.fishing_api.domain.user.service.UserService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/v1/users")
public class UserController {

  private final UserService userService;
  private final RefreshTokenCookieManager refreshTokenCookieManager;

  public UserController(
      UserService userService, RefreshTokenCookieManager refreshTokenCookieManager) {
    this.userService = userService;
    this.refreshTokenCookieManager = refreshTokenCookieManager;
  }

  @GetMapping("/me")
  public ApiResponse<AuthUserResponse> getMe(@CurrentUser AuthenticatedUser user) {
    return ApiResponse.success(userService.getMe(user.id()));
  }

  @GetMapping("/nickname/check")
  public ApiResponse<NicknameCheckResponse> checkNickname(
      @RequestParam @NotBlank @Size(min = 2, max = 10) String nickname) {
    return ApiResponse.success(
        new NicknameCheckResponse(userService.isNicknameAvailable(nickname)));
  }

  @PostMapping("/me/profile")
  public ApiResponse<AuthTokenResponse> completeProfile(
      @CurrentUser AuthenticatedUser user,
      @Valid @RequestBody CompleteProfileRequest request,
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse) {
    AuthSessionResult result =
        userService.completeProfile(
            user.id(), request.nickname(), httpServletRequest.getHeader("User-Agent"));
    refreshTokenCookieManager.addRefreshTokenCookie(
        httpServletResponse, result.refreshToken(), result.response().refreshTokenExpiresAt());
    return ApiResponse.success(result.response());
  }

  @PutMapping("/me/nickname")
  public ApiResponse<AuthUserResponse> updateNickname(
      @CurrentUser AuthenticatedUser user, @Valid @RequestBody UpdateNicknameRequest request) {
    return ApiResponse.success(userService.updateNickname(user.id(), request.nickname()));
  }

  @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<AuthUserResponse> updateProfileImage(
      @CurrentUser AuthenticatedUser user, @RequestPart("image") MultipartFile image) {
    return ApiResponse.success(userService.updateProfileImage(user.id(), image));
  }

  @DeleteMapping("/me/profile-image")
  public ApiResponse<AuthUserResponse> resetProfileImage(@CurrentUser AuthenticatedUser user) {
    return ApiResponse.success(userService.resetProfileImage(user.id()));
  }

  @GetMapping("/{userId}")
  public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
    return ApiResponse.success(userService.getUserProfile(userId));
  }
}
