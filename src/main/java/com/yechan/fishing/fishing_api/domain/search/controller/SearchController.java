package com.yechan.fishing.fishing_api.domain.search.controller;

import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostsRequest;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostsResponse;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchRegionCountItem;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchRegionCountsRequest;
import com.yechan.fishing.fishing_api.domain.search.service.SearchService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/posts")
    public ApiResponse<SearchPostsResponse> searchPosts(@Valid SearchPostsRequest request) {
        return ApiResponse.success(searchService.searchPosts(request));
    }

    @GetMapping("/regions")
    public ApiResponse<List<SearchRegionCountItem>> getRegionCounts(@Valid SearchRegionCountsRequest request) {
        return ApiResponse.success(searchService.getRegionCounts(request.fromDate(), request.untilDate()));
    }
}
