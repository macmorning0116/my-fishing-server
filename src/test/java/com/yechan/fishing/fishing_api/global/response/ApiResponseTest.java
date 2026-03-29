package com.yechan.fishing.fishing_api.global.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    void success_createsSuccessfulResponse() {
        ApiResponse<String> response = ApiResponse.success("payload");

        assertTrue(response.isSuccess());
        assertEquals("payload", response.getData());
        assertNull(response.getError());
    }

    @Test
    void fail_createsErrorResponse() {
        ApiResponse<Void> response = ApiResponse.fail("ERR", "failed");

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("ERR", response.getError().getCode());
        assertEquals("failed", response.getError().getMessage());
    }
}
