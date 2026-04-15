package com.yechan.fishing.fishing_api.domain.search.controller;

import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostsRequest;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchRegionCountItem;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchRegionCountsRequest;
import com.yechan.fishing.fishing_api.domain.search.dto.UnifiedSearchResponse;
import com.yechan.fishing.fishing_api.domain.search.service.CommunitySearchService;
import com.yechan.fishing.fishing_api.domain.search.service.SearchService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/search")
public class SearchController {

  private final SearchService searchService;
  private final CommunitySearchService communitySearchService;

  public SearchController(
      SearchService searchService, CommunitySearchService communitySearchService) {
    this.searchService = searchService;
    this.communitySearchService = communitySearchService;
  }

  @GetMapping("/posts")
  public ApiResponse<UnifiedSearchResponse> searchPosts(@Valid SearchPostsRequest request) {
    return ApiResponse.success(searchService.searchUnified(request));
  }

  @GetMapping("/regions")
  public ApiResponse<List<SearchRegionCountItem>> getRegionCounts(
      @Valid SearchRegionCountsRequest request) {
    return ApiResponse.success(
        searchService.getRegionCounts(request.fromDate(), request.untilDate()));
  }

  @PostMapping("/community/reindex")
  public ApiResponse<Map<String, Integer>> reindexCommunityPosts() {
    int count = communitySearchService.reindexAll();
    return ApiResponse.success(Map.of("indexed", count));
  }
}
