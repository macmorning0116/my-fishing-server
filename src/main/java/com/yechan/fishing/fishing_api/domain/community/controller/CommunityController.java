package com.yechan.fishing.fishing_api.domain.community.controller;

import com.yechan.fishing.fishing_api.domain.auth.security.AuthenticatedUser;
import com.yechan.fishing.fishing_api.domain.auth.security.CurrentUser;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityCommentItem;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityCommentsRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityCommentsResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityLikeResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostDefaultsResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostDetailResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostsRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostsResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityRegionCountItem;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityReportRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityReportResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CreateCommunityCommentRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CreateCommunityPostRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.MapPostItem;
import com.yechan.fishing.fishing_api.domain.community.dto.UpdateCommunityCommentRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.UpdateCommunityPostRequest;
import com.yechan.fishing.fishing_api.domain.community.service.CommunityPostDefaultsService;
import com.yechan.fishing.fishing_api.domain.community.service.CommunityService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/v1/community")
public class CommunityController {

  private final CommunityService communityService;
  private final CommunityPostDefaultsService communityPostDefaultsService;

  public CommunityController(
      CommunityService communityService,
      CommunityPostDefaultsService communityPostDefaultsService) {
    this.communityService = communityService;
    this.communityPostDefaultsService = communityPostDefaultsService;
  }

  @GetMapping("/posts/map")
  public ApiResponse<List<MapPostItem>> getMapPosts() {
    return ApiResponse.success(communityService.getAllMapPosts());
  }

  @GetMapping("/posts/region-counts")
  public ApiResponse<List<CommunityRegionCountItem>> getRegionCounts() {
    return ApiResponse.success(communityService.getRegionCounts());
  }

  @GetMapping("/posts")
  public ApiResponse<CommunityPostsResponse> getPosts(
      @Valid CommunityPostsRequest request, @CurrentUser(required = false) AuthenticatedUser user) {
    return ApiResponse.success(communityService.getPosts(request, currentUserId(user)));
  }

  @GetMapping("/posts/{postId}")
  public ApiResponse<CommunityPostDetailResponse> getPost(
      @PathVariable Long postId, @CurrentUser(required = false) AuthenticatedUser user) {
    return ApiResponse.success(communityService.getPost(postId, currentUserId(user)));
  }

  @PostMapping(value = "/posts/defaults", consumes = "multipart/form-data")
  public ApiResponse<CommunityPostDefaultsResponse> getPostDefaults(
      @RequestPart("image") MultipartFile image) {
    return ApiResponse.success(communityPostDefaultsService.extractDefaults(image));
  }

  @PostMapping(value = "/posts", consumes = "multipart/form-data")
  public ApiResponse<CommunityPostDetailResponse> createPost(
      @CurrentUser AuthenticatedUser user,
      @Valid @RequestPart("request") CreateCommunityPostRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images) {
    return ApiResponse.success(communityService.createPost(user.id(), request, images));
  }

  @PutMapping(value = "/posts/{postId}", consumes = "multipart/form-data")
  public ApiResponse<CommunityPostDetailResponse> editPost(
      @PathVariable Long postId,
      @CurrentUser AuthenticatedUser user,
      @Valid @RequestPart("request") UpdateCommunityPostRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images) {
    return ApiResponse.success(communityService.editPost(postId, user.id(), request, images));
  }

  @DeleteMapping("/posts/{postId}")
  public ApiResponse<Void> deletePost(
      @PathVariable Long postId, @CurrentUser AuthenticatedUser user) {
    communityService.deletePost(postId, user.id());
    return ApiResponse.success(null);
  }

  @GetMapping("/posts/{postId}/comments")
  public ApiResponse<CommunityCommentsResponse> getComments(
      @PathVariable Long postId, @Valid CommunityCommentsRequest request) {
    return ApiResponse.success(communityService.getComments(postId, request));
  }

  @PostMapping("/posts/{postId}/comments")
  public ApiResponse<CommunityCommentItem> createComment(
      @PathVariable Long postId,
      @CurrentUser AuthenticatedUser user,
      @Valid @RequestBody CreateCommunityCommentRequest request) {
    return ApiResponse.success(communityService.createComment(postId, user.id(), request));
  }

  @PostMapping("/posts/{postId}/likes")
  public ApiResponse<CommunityLikeResponse> likePost(
      @PathVariable Long postId, @CurrentUser AuthenticatedUser user) {
    return ApiResponse.success(communityService.likePost(postId, user.id()));
  }

  @DeleteMapping("/posts/{postId}/likes")
  public ApiResponse<CommunityLikeResponse> unlikePost(
      @PathVariable Long postId, @CurrentUser AuthenticatedUser user) {
    return ApiResponse.success(communityService.unlikePost(postId, user.id()));
  }

  @PostMapping("/posts/{postId}/reports")
  public ApiResponse<CommunityReportResponse> reportPost(
      @PathVariable Long postId,
      @CurrentUser AuthenticatedUser user,
      @Valid @RequestBody CommunityReportRequest request) {
    return ApiResponse.success(communityService.reportPost(postId, user.id(), request));
  }

  @PutMapping("/comments/{commentId}")
  public ApiResponse<CommunityCommentItem> editComment(
      @PathVariable Long commentId,
      @CurrentUser AuthenticatedUser user,
      @Valid @RequestBody UpdateCommunityCommentRequest request) {
    return ApiResponse.success(
        communityService.editComment(commentId, user.id(), request.content()));
  }

  @DeleteMapping("/comments/{commentId}")
  public ApiResponse<Void> deleteComment(
      @PathVariable Long commentId, @CurrentUser AuthenticatedUser user) {
    communityService.deleteComment(commentId, user.id());
    return ApiResponse.success(null);
  }

  @PostMapping("/comments/{commentId}/reports")
  public ApiResponse<CommunityReportResponse> reportComment(
      @PathVariable Long commentId,
      @CurrentUser AuthenticatedUser user,
      @Valid @RequestBody CommunityReportRequest request) {
    return ApiResponse.success(communityService.reportComment(commentId, user.id(), request));
  }

  private Long currentUserId(AuthenticatedUser user) {
    return user == null ? null : user.id();
  }
}
