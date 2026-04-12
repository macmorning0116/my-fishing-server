package com.yechan.fishing.fishing_api.global.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleFishingException_returnsFailResponseWithProperStatus() {
    ResponseEntity<ApiResponse<Void>> responseEntity =
        handler.handleFishingException(new FishingException(ErrorCode.WEATHER_API_ERROR));

    assertEquals(502, responseEntity.getStatusCode().value());

    ApiResponse<Void> response = responseEntity.getBody();
    assertFalse(response.isSuccess());
    assertNull(response.getData());
    assertEquals("WEATHER_API_ERROR", response.getError().getCode());
    assertEquals("날씨 정보를 불러오는 과정 중 에러가 발생했습니다.", response.getError().getMessage());
  }

  @Test
  void handleFishingException_authError_returns401() {
    ResponseEntity<ApiResponse<Void>> responseEntity =
        handler.handleFishingException(new FishingException(ErrorCode.AUTH_INVALID_TOKEN));

    assertEquals(401, responseEntity.getStatusCode().value());
  }

  @Test
  void handleFishingException_notFound_returns404() {
    ResponseEntity<ApiResponse<Void>> responseEntity =
        handler.handleFishingException(new FishingException(ErrorCode.USER_NOT_FOUND));

    assertEquals(404, responseEntity.getStatusCode().value());
  }
}
