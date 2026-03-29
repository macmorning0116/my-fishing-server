package com.yechan.fishing.fishing_api.global.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void doFilterInternal_setsRequestIdDuringRequestAndClearsMdcAfterwards() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/weather");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            String requestId = org.slf4j.MDC.get("requestId");
            assertTrue(requestId != null && requestId.length() == 8);
            ((MockHttpServletResponse) res).setStatus(204);
        };

        filter.doFilter(request, response, chain);

        assertEqualsStatus(204, response.getStatus());
        assertNull(org.slf4j.MDC.get("requestId"));
    }

    @Test
    void shouldNotFilter_usesDefaultBehavior() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/anything");

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", request);

        assertFalse(result);
    }

    private void assertEqualsStatus(int expected, int actual) {
        assertTrue(expected == actual);
    }
}
