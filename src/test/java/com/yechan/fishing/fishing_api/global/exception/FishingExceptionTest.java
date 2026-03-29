package com.yechan.fishing.fishing_api.global.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishingExceptionTest {

    @Test
    void constructor_exposesErrorCodeAndMessage() {
        FishingException exception = new FishingException(ErrorCode.GPT_API_ERROR);

        assertEquals(ErrorCode.GPT_API_ERROR, exception.getErrorCode());
        assertEquals("GPT 응답을 불러오는 중 에러가 발생했습니다.", exception.getMessage());
    }
}
