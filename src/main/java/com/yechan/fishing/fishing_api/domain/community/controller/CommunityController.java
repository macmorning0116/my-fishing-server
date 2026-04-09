package com.yechan.fishing.fishing_api.domain.community.controller;

import com.yechan.fishing.fishing_api.domain.community.dto.CommunityCommentItem;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityLikeRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityLikeResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostDetailResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostsRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostsResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityReportRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityReportResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CreateCommunityCommentRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CreateCommunityPostRequest;
import com.yechan.fishing.fishing_api.domain.community.service.CommunityService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/community")
public class CommunityController {

  private final CommunityService communityService;

  public CommunityController(CommunityService communityService) {
    this.communityService = communityService;
  }

  @GetMapping("/posts")
  public ApiResponse<CommunityPostsResponse> getPosts(@Valid CommunityPostsRequest request) {
    return ApiResponse.success(communityService.getPosts(request));
  }

  @GetMapping("/posts/{postId}")
  public ApiResponse<CommunityPostDetailResponse> getPost(
      @PathVariable Long postId, @RequestParam(required = false) Long viewerUserId) {
    return ApiResponse.success(communityService.getPost(postId, viewerUserId));
  }

  @PostMapping("/posts")
  public ApiResponse<CommunityPostDetailResponse> createPost(
      @Valid @RequestBody CreateCommunityPostRequest request) {
    return ApiResponse.success(communityService.createPost(request));
  }

  @GetMapping("/posts/{postId}/comments")
  public ApiResponse<List<CommunityCommentItem>> getComments(@PathVariable Long postId) {
    return ApiResponse.success(communityService.getComments(postId));
  }

  @PostMapping("/posts/{postId}/comments")
  public ApiResponse<CommunityCommentItem> createComment(
      @PathVariable Long postId, @Valid @RequestBody CreateCommunityCommentRequest request) {
    return ApiResponse.success(communityService.createComment(postId, request));
  }

  @PostMapping("/posts/{postId}/likes")
  public ApiResponse<CommunityLikeResponse> likePost(
      @PathVariable Long postId, @Valid @RequestBody CommunityLikeRequest request) {
    return ApiResponse.success(communityService.likePost(postId, request.userId()));
  }

  @DeleteMapping("/posts/{postId}/likes")
  public ApiResponse<CommunityLikeResponse> unlikePost(
      @PathVariable Long postId, @RequestParam @NotNull Long userId) {
    return ApiResponse.success(communityService.unlikePost(postId, userId));
  }

  @PostMapping("/posts/{postId}/reports")
  public ApiResponse<CommunityReportResponse> reportPost(
      @PathVariable Long postId, @Valid @RequestBody CommunityReportRequest request) {
    return ApiResponse.success(communityService.reportPost(postId, request));
  }

  @PostMapping("/comments/{commentId}/reports")
  public ApiResponse<CommunityReportResponse> reportComment(
      @PathVariable Long commentId, @Valid @RequestBody CommunityReportRequest request) {
    return ApiResponse.success(communityService.reportComment(commentId, request));
  }
}
