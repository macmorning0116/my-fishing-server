package com.yechan.fishing.fishing_api.domain.search.controller;

import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostItem;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostsResponse;
import com.yechan.fishing.fishing_api.domain.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Test
    void searchPosts_returnsWrappedSuccessResponse() throws Exception {
        SearchPostsResponse response = new SearchPostsResponse(
                List.of(new SearchPostItem(
                        "469820",
                        "효마지 비온 뒤 출쫌,,",
                        "https://example.com/articles/469820",
                        "초보초보",
                        "2026-04-01",
                        "bass_walking",
                        "배스 조행기(워킹조행)",
                        "배스",
                        "경상권",
                        "효마지수지",
                        "ok",
                        List.of("배스", "경상권", "효마지수지")
                )),
                1L,
                20,
                "next-cursor"
        );

        given(searchService.searchPosts(argThat(request ->
                "bass".equals(request.q())
                        && "bass_walking".equals(request.boardKey())
                        && LocalDate.of(2026, 3, 28).equals(request.fromDate())
                        && LocalDate.of(2026, 4, 1).equals(request.untilDate())
                        && request.cursor() == null
                        && Integer.valueOf(20).equals(request.size())
        ))).willReturn(response);

        mockMvc.perform(get("/v1/search/posts")
                        .param("q", "bass")
                        .param("boardKey", "bass_walking")
                        .param("fromDate", "2026-03-28")
                        .param("untilDate", "2026-04-01")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].articleId").value("469820"))
                .andExpect(jsonPath("$.data.items[0].publishedAt").value("2026-04-01"))
                .andExpect(jsonPath("$.data.items[0].tags[0]").value("배스"));
    }

    @Test
    void searchPosts_whenSizeIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/v1/search/posts")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }
}
