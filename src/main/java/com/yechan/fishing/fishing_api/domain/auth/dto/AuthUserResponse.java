package com.yechan.fishing.fishing_api.domain.auth.dto;

import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserRole;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserStatus;
import java.time.LocalDateTime;

public record AuthUserResponse(
    Long id,
    String nickname,
    String profileImageUrl,
    UserRole role,
    UserStatus status,
    boolean needsProfileSetup,
    String email,
    AuthProvider provider,
    LocalDateTime nicknameChangedAt) {}
