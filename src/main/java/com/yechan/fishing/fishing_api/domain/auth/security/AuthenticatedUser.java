package com.yechan.fishing.fishing_api.domain.auth.security;

import com.yechan.fishing.fishing_api.domain.auth.entity.enums.UserRole;

public record AuthenticatedUser(Long id, UserRole role) {}
