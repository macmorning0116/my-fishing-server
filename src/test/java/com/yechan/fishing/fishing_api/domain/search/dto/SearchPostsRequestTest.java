package com.yechan.fishing.fishing_api.domain.search.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SearchPostsRequestTest {

    @Test
    void safeSize_returnsDefaultWhenSizeIsNull() {
        SearchPostsRequest request = new SearchPostsRequest(
                "bass",
                "bass_walking",
                LocalDate.of(2026, 3, 28),
                LocalDate.of(2026, 4, 1),
                null,
                null
        );

        assertEquals(20, request.safeSize());
        assertEquals("bass", request.q());
        assertEquals("bass_walking", request.boardKey());
        assertEquals(LocalDate.of(2026, 3, 28), request.fromDate());
        assertEquals(LocalDate.of(2026, 4, 1), request.untilDate());
        assertNull(request.cursor());
    }

    @Test
    void safeSize_returnsProvidedSize() {
        SearchPostsRequest request = new SearchPostsRequest(null, null, null, null, "cursor", 50);

        assertEquals(50, request.safeSize());
    }
}
