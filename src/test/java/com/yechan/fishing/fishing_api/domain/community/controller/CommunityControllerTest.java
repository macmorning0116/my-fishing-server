package com.yechan.fishing.fishing_api.domain.community.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.jwt.AccessTokenPayload;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.domain.auth.security.CurrentUserArgumentResolver;
import com.yechan.fishing.fishing_api.domain.auth.security.JwtAuthenticationFilter;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityLikeResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostDefaultsResponse;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.FishedAtSource;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.LocationSource;
import com.yechan.fishing.fishing_api.domain.community.service.CommunityPostDefaultsService;
import com.yechan.fishing.fishing_api.domain.community.service.CommunityService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(CommunityController.class)
@Import(CommunityControllerTest.AuthTestConfig.class)
class CommunityControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CommunityService communityService;

  @MockBean private CommunityPostDefaultsService communityPostDefaultsService;

  @MockBean private UserRepository userRepository;

  @TestConfiguration
  static class AuthTestConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;

    AuthTestConfig(UserRepository userRepository) {
      this.userRepository = userRepository;
    }

    @Override
    public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
      resolvers.add(new CurrentUserArgumentResolver(userRepository));
    }
  }

  @Test
  void getPostDefaults_returnsWrappedSuccessResponse() throws Exception {
    MockMultipartFile image =
        new MockMultipartFile("image", "spot.jpg", "image/jpeg", "image".getBytes());
    CommunityPostDefaultsResponse response =
        new CommunityPostDefaultsResponse(
            LocalDateTime.of(2026, 4, 9, 6, 30),
            FishedAtSource.EXIF,
            37.5665,
            126.9780,
            LocationSource.EXIF,
            "서울/경기권",
            "서울특별시 중구 태평로1가");

    given(communityPostDefaultsService.extractDefaults(any())).willReturn(response);

    mockMvc
        .perform(multipart("/v1/community/posts/defaults").file(image))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fishedAt").value("2026-04-09T06:30:00"))
        .andExpect(jsonPath("$.data.fishedAtSource").value("EXIF"))
        .andExpect(jsonPath("$.data.latitude").value(37.5665))
        .andExpect(jsonPath("$.data.longitude").value(126.978))
        .andExpect(jsonPath("$.data.locationSource").value("EXIF"))
        .andExpect(jsonPath("$.data.region").value("서울/경기권"))
        .andExpect(jsonPath("$.data.placeName").value("서울특별시 중구 태평로1가"));
  }

  @Test
  void getPostDefaults_whenImageIsMissing_returnsBadRequest() throws Exception {
    mockMvc.perform(multipart("/v1/community/posts/defaults")).andExpect(status().isBadRequest());
  }

  @Test
  void likePost_withAuthenticatedUserAttribute_usesAuthenticatedUserId() throws Exception {
    User user =
        User.create(
            AuthProvider.KAKAO, "123456", "angler@example.com", "앵글러", null, LocalDateTime.now());
    ReflectionTestUtils.setField(user, "id", 1L);

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(communityService.likePost(10L, 1L)).willReturn(new CommunityLikeResponse(true, 3));

    mockMvc
        .perform(
            post("/v1/community/posts/10/likes")
                .requestAttr(
                    JwtAuthenticationFilter.AUTHENTICATED_ACCESS_TOKEN_ATTRIBUTE,
                    new AccessTokenPayload(
                        1L,
                        user.getRole(),
                        user.getProvider(),
                        LocalDateTime.now().plusMinutes(30))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.liked").value(true))
        .andExpect(jsonPath("$.data.likeCount").value(3));

    verify(communityService).likePost(10L, 1L);
  }

  @Test
  void likePost_withoutAccessToken_returnsLoginRequired() throws Exception {
    mockMvc
        .perform(post("/v1/community/posts/10/likes"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("AUTH_LOGIN_REQUIRED"));
  }
}
