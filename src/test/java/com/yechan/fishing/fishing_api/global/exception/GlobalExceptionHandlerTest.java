package com.yechan.fishing.fishing_api.global.exception;

import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleFishingException_returnsFailResponse() {
        ApiResponse<Void> response = handler.handleFishingException(
                new FishingException(ErrorCode.WEATHER_API_ERROR)
        );

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("WEATHER_API_ERROR", response.getError().getCode());
        assertEquals("날씨 정보를 불러오는 과정 중 에러가 발생했습니다.", response.getError().getMessage());
    }
}
