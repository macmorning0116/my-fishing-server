package com.yechan.fishing.fishing_api.domain.search.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SearchPostsRequestTest {

  @Test
  void safeSize_returnsDefaultWhenSizeIsNull() {
    SearchPostsRequest request =
        new SearchPostsRequest(
            "bass",
            "bass_walking",
            "경상권",
            LocalDate.of(2026, 3, 28),
            LocalDate.of(2026, 4, 1),
            null,
            null,
            null);

    assertEquals(20, request.safeSize());
    assertEquals("all", request.safeSource());
    assertEquals("bass", request.q());
    assertEquals("bass_walking", request.boardKey());
    assertEquals("경상권", request.region());
    assertEquals(LocalDate.of(2026, 3, 28), request.fromDate());
    assertEquals(LocalDate.of(2026, 4, 1), request.untilDate());
    assertNull(request.cursor());
  }

  @Test
  void safeSize_returnsProvidedSize() {
    SearchPostsRequest request =
        new SearchPostsRequest(null, null, null, null, null, "cursor", 50, null);

    assertEquals(50, request.safeSize());
  }
}
