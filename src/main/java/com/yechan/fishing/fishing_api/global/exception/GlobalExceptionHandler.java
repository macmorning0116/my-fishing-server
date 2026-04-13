package com.yechan.fishing.fishing_api.global.exception;

import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(FishingException.class)
  public ResponseEntity<ApiResponse<Void>> handleFishingException(FishingException e) {
    ErrorCode ec = e.getErrorCode();
    return ResponseEntity.status(ec.getHttpStatus())
        .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .orElse("잘못된 요청입니다.");
    return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION_ERROR", message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
      ConstraintViolationException e) {
    String message =
        e.getConstraintViolations().stream()
            .findFirst()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .orElse("잘못된 요청입니다.");
    return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION_ERROR", message));
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingPart(MissingServletRequestPartException e) {
    return ResponseEntity.badRequest()
        .body(ApiResponse.fail("VALIDATION_ERROR", e.getRequestPartName() + " 항목이 필요합니다."));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception e) {
    log.error("Unhandled exception", e);
    return ResponseEntity.status(500)
        .body(ApiResponse.fail("INTERNAL_SERVER_ERROR", e.getMessage()));
  }
}
