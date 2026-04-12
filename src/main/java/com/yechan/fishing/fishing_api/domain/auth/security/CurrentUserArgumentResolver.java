package com.yechan.fishing.fishing_api.domain.auth.security;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.jwt.AccessTokenPayload;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

  private final UserRepository userRepository;

  public CurrentUserArgumentResolver(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(CurrentUser.class)
        && AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    CurrentUser currentUser = parameter.getParameterAnnotation(CurrentUser.class);
    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    AccessTokenPayload payload =
        request == null
            ? null
            : (AccessTokenPayload)
                request.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_ACCESS_TOKEN_ATTRIBUTE);

    if (payload == null) {
      if (currentUser != null && !currentUser.required()) {
        return null;
      }
      throw new FishingException(ErrorCode.AUTH_LOGIN_REQUIRED);
    }

    User user =
        userRepository
            .findById(payload.userId())
            .orElseThrow(() -> new FishingException(ErrorCode.AUTH_INVALID_TOKEN));

    if (!user.isPending()) {
      user.ensureActive();
    }

    System.out.println(
        "[DEBUG] CurrentUserArgumentResolver - userId=" + user.getId() + " role=" + user.getRole());
    return new AuthenticatedUser(user.getId(), user.getRole());
  }
}
