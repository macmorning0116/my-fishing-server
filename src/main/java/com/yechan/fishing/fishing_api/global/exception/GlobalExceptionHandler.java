package com.yechan.fishing.fishing_api.global.exception;

import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(FishingException.class)
  public ResponseEntity<ApiResponse<Void>> handleFishingException(FishingException e) {
    ErrorCode ec = e.getErrorCode();
    return ResponseEntity.status(ec.getHttpStatus())
        .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e) {
    log.error("Unhandled exception", e);
    // 디버깅: 스택트레이스를 파일에 저장
    try {
      java.io.PrintWriter pw = new java.io.PrintWriter("/tmp/fishing-error.log");
      e.printStackTrace(pw);
      pw.close();
    } catch (Exception ignored) {
    }
    return ResponseEntity.status(500)
        .body(ApiResponse.fail("INTERNAL_SERVER_ERROR", e.getMessage()));
  }
}
