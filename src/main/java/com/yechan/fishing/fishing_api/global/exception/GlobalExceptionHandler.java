package com.yechan.fishing.fishing_api.global.exception;

import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FishingException.class)
    public ApiResponse<Void> handleFishingException(FishingException e) {
        ErrorCode ec = e.getErrorCode();
        return ApiResponse.fail(ec.getCode(), ec.getMessage());
    }
}
