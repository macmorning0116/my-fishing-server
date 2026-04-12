package com.yechan.fishing.fishing_api.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(@NotBlank @Size(min = 2, max = 10) String nickname) {}
