package com.yechan.fishing.fishing_api.domain.community.service;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityCommentItem;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityCommentsRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityCommentsResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityLikeResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostDetailResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostImageItem;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostItem;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostSummaryItem;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostsRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityPostsResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityRegionCountItem;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityReportRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CommunityReportResponse;
import com.yechan.fishing.fishing_api.domain.community.dto.CreateCommunityCommentRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.CreateCommunityPostRequest;
import com.yechan.fishing.fishing_api.domain.community.dto.MapPostItem;
import com.yechan.fishing.fishing_api.domain.community.dto.UpdateCommunityPostRequest;
import com.yechan.fishing.fishing_api.domain.community.entity.CommunityComment;
import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPost;
import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPostImage;
import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPostLike;
import com.yechan.fishing.fishing_api.domain.community.entity.CommunityReport;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.ReportStatus;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.ReportTargetType;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityReason;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import com.yechan.fishing.fishing_api.domain.community.repository.CommunityCommentRepository;
import com.yechan.fishing.fishing_api.domain.community.repository.CommunityPostImageRepository;
import com.yechan.fishing.fishing_api.domain.community.repository.CommunityPostLikeRepository;
import com.yechan.fishing.fishing_api.domain.community.repository.CommunityPostRepository;
import com.yechan.fishing.fishing_api.domain.community.repository.CommunityReportRepository;
import com.yechan.fishing.fishing_api.domain.community.storage.ImageStorageService;
import com.yechan.fishing.fishing_api.domain.community.storage.StoredCommunityImage;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CommunityService {

  private static final int REPORT_HIDE_THRESHOLD = 5;
  private static final GeometryFactory GEOMETRY_FACTORY =
      new GeometryFactory(new PrecisionModel(), 4326);

  private final UserRepository userRepository;
  private final CommunityPostRepository communityPostRepository;
  private final CommunityPostImageRepository communityPostImageRepository;
  private final CommunityPostLikeRepository communityPostLikeRepository;
  private final CommunityCommentRepository communityCommentRepository;
  private final CommunityReportRepository communityReportRepository;
  private final ImageStorageService imageStorageService;

  public CommunityService(
      UserRepository userRepository,
      CommunityPostRepository communityPostRepository,
      CommunityPostImageRepository communityPostImageRepository,
      CommunityPostLikeRepository communityPostLikeRepository,
      CommunityCommentRepository communityCommentRepository,
      CommunityReportRepository communityReportRepository,
      ImageStorageService imageStorageService) {
    this.userRepository = userRepository;
    this.communityPostRepository = communityPostRepository;
    this.communityPostImageRepository = communityPostImageRepository;
    this.communityPostLikeRepository = communityPostLikeRepository;
    this.communityCommentRepository = communityCommentRepository;
    this.communityReportRepository = communityReportRepository;
    this.imageStorageService = imageStorageService;
  }

  @Transactional(readOnly = true)
  public CommunityPostsResponse getPosts(CommunityPostsRequest request, Long viewerUserId) {
    int size = request.safeSize();
    List<CommunityPost> posts;
    if (request.authorId() != null) {
      posts =
          request.cursor() == null
              ? communityPostRepository.findAllWithUserByUserIdAndVisibilityStatus(
                  request.authorId(), VisibilityStatus.VISIBLE, PageRequest.of(0, size))
              : communityPostRepository.findAllWithUserByUserIdAndVisibilityStatusAndIdLessThan(
                  request.authorId(),
                  VisibilityStatus.VISIBLE,
                  request.cursor(),
                  PageRequest.of(0, size));
    } else {
      posts =
          request.cursor() == null
              ? communityPostRepository.findAllWithUserByVisibilityStatus(
                  VisibilityStatus.VISIBLE, PageRequest.of(0, size))
              : communityPostRepository.findAllWithUserByVisibilityStatusAndIdLessThan(
                  VisibilityStatus.VISIBLE, request.cursor(), PageRequest.of(0, size));
    }

    List<Long> postIds = posts.stream().map(CommunityPost::getId).toList();
    Map<Long, Boolean> likedByPostId = loadLikedByPostId(viewerUserId, postIds);

    List<CommunityPostSummaryItem> items = new ArrayList<>();
    for (CommunityPost post : posts) {
      items.add(toSummaryItem(post, likedByPostId.getOrDefault(post.getId(), false)));
    }

    Long nextCursor = items.size() == size ? items.get(items.size() - 1).id() : null;
    return new CommunityPostsResponse(items, size, nextCursor);
  }

  @Transactional(readOnly = true)
  public CommunityPostDetailResponse getPost(Long postId, Long viewerUserId) {
    CommunityPost post = getVisiblePost(postId);
    List<CommunityPostImage> images =
        communityPostImageRepository.findAllByPost_IdOrderBySortOrderAsc(postId);
    boolean likedByMe =
        viewerUserId != null
            && communityPostLikeRepository.existsByPost_IdAndUser_Id(postId, viewerUserId);

    return new CommunityPostDetailResponse(
        toPostItem(post, likedByMe, images), toImageItems(images));
  }

  @Transactional
  public CommunityPostDetailResponse createPost(
      Long userId, CreateCommunityPostRequest request, List<MultipartFile> imageFiles) {
    User user = getUser(userId);
    LocalDateTime now = LocalDateTime.now();
    List<StoredCommunityImage> storedImages = imageStorageService.storeCommunityImages(imageFiles);

    CommunityPost post =
        communityPostRepository.save(
            CommunityPost.builder()
                .user(user)
                .content(request.content().trim())
                .region(request.region().trim())
                .placeName(request.placeName())
                .location(toPoint(request.latitude(), request.longitude()))
                .fishedAt(request.fishedAt())
                .fishedAtSource(request.fishedAtSource())
                .locationSource(request.locationSource())
                .species(request.species())
                .lengthCm(request.lengthCm())
                .tackleType(request.tackleType())
                .tackleCustomText(request.tackleCustomText())
                .thumbnailImageUrl(firstUploadedImageUrl(storedImages))
                .likeCount(0)
                .commentCount(0)
                .reportCount(0)
                .visibilityStatus(VisibilityStatus.VISIBLE)
                .createdAt(now)
                .updatedAt(now)
                .build());

    List<CommunityPostImage> images = createImages(post, storedImages, now);
    if (!images.isEmpty()) {
      communityPostImageRepository.saveAll(images);
    }

    return new CommunityPostDetailResponse(toPostItem(post, false, images), toImageItems(images));
  }

  @Transactional(readOnly = true)
  public CommunityCommentsResponse getComments(Long postId, CommunityCommentsRequest request) {
    getVisiblePost(postId);
    int size = request.safeSize();

    List<CommunityComment> comments;
    if (request.cursor() == null) {
      comments =
          communityCommentRepository.findFirstPageWithRelationsByPostId(
              postId, PageRequest.of(0, size));
    } else {
      comments =
          communityCommentRepository.findAllWithRelationsByPostIdAndIdGreaterThan(
              postId, request.cursor(), PageRequest.of(0, size));
    }

    List<CommunityCommentItem> items = comments.stream().map(this::toCommentItem).toList();
    Long nextCursor = items.size() == size ? items.get(items.size() - 1).id() : null;
    return new CommunityCommentsResponse(items, size, nextCursor);
  }

  @Transactional
  public CommunityCommentItem createComment(
      Long postId, Long userId, CreateCommunityCommentRequest request) {
    CommunityPost post = getVisiblePost(postId);
    User user = getUser(userId);
    CommunityComment parentComment = null;

    if (request.parentCommentId() != null) {
      parentComment =
          communityCommentRepository
              .findByIdAndPost_Id(request.parentCommentId(), postId)
              .orElseThrow(() -> new FishingException(ErrorCode.COMMUNITY_INVALID_PARENT_COMMENT));
    }

    User replyToUser =
        request.replyToUserId() == null
            ? (parentComment == null ? null : parentComment.getUser())
            : getUser(request.replyToUserId());

    LocalDateTime now = LocalDateTime.now();
    CommunityComment comment =
        communityCommentRepository.save(
            CommunityComment.builder()
                .post(post)
                .user(user)
                .parentComment(parentComment)
                .replyToUser(replyToUser)
                .content(request.content().trim())
                .likeCount(0)
                .reportCount(0)
                .visibilityStatus(VisibilityStatus.VISIBLE)
                .createdAt(now)
                .updatedAt(now)
                .build());

    communityPostRepository.incrementCommentCount(post.getId());
    return toCommentItem(comment);
  }

  @Transactional
  public CommunityLikeResponse likePost(Long postId, Long userId) {
    CommunityPost post = getVisiblePost(postId);
    User user = getUser(userId);

    if (!communityPostLikeRepository.existsByPost_IdAndUser_Id(postId, userId)) {
      communityPostLikeRepository.save(
          CommunityPostLike.builder().post(post).user(user).createdAt(LocalDateTime.now()).build());
      communityPostRepository.incrementLikeCount(postId);
      return new CommunityLikeResponse(true, post.getLikeCount() + 1);
    }

    return new CommunityLikeResponse(true, post.getLikeCount());
  }

  @Transactional
  public CommunityLikeResponse unlikePost(Long postId, Long userId) {
    CommunityPost post = getVisiblePost(postId);

    boolean removed =
        communityPostLikeRepository
            .findByPost_IdAndUser_Id(postId, userId)
            .map(
                like -> {
                  communityPostLikeRepository.delete(like);
                  communityPostRepository.decrementLikeCount(postId);
                  return true;
                })
            .orElse(false);

    int likeCount = removed ? Math.max(0, post.getLikeCount() - 1) : post.getLikeCount();
    return new CommunityLikeResponse(false, likeCount);
  }

  @Transactional
  public CommunityReportResponse reportPost(
      Long postId, Long reporterUserId, CommunityReportRequest request) {
    CommunityPost post = getPostOrThrow(postId);
    CommunityReport report = createReport(ReportTargetType.POST, postId, reporterUserId, request);

    communityPostRepository.incrementReportCount(postId);
    applyAutoHide(post);

    return new CommunityReportResponse(
        report.getId(), report.getStatus(), post.getVisibilityStatus());
  }

  @Transactional
  public CommunityReportResponse reportComment(
      Long commentId, Long reporterUserId, CommunityReportRequest request) {
    CommunityComment comment = getCommentOrThrow(commentId);
    CommunityReport report =
        createReport(ReportTargetType.COMMENT, commentId, reporterUserId, request);

    communityCommentRepository.incrementReportCount(commentId);
    applyAutoHide(comment);

    return new CommunityReportResponse(
        report.getId(), report.getStatus(), comment.getVisibilityStatus());
  }

  private CommunityReport createReport(
      ReportTargetType targetType,
      Long targetId,
      Long reporterUserId,
      CommunityReportRequest request) {
    User reporter = getUser(reporterUserId);

    if (communityReportRepository.existsByReporterUser_IdAndTargetTypeAndTargetId(
        reporter.getId(), targetType, targetId)) {
      throw new FishingException(ErrorCode.COMMUNITY_REPORT_DUPLICATE);
    }

    CommunityReport report =
        communityReportRepository.save(
            CommunityReport.builder()
                .reporterUser(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .reasonType(request.reasonType())
                .reasonDetail(request.reasonDetail())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

    if (targetType == ReportTargetType.USER) {
      getUser(targetId).incrementReportCount();
    }

    return report;
  }

  private void applyAutoHide(CommunityPost post) {
    long distinctReporterCount =
        communityReportRepository.countDistinctReporterUser_IdByTargetTypeAndTargetId(
            ReportTargetType.POST, post.getId());
    if (distinctReporterCount >= REPORT_HIDE_THRESHOLD
        && post.getVisibilityStatus() == VisibilityStatus.VISIBLE) {
      post.hide(VisibilityReason.REPORT_THRESHOLD, LocalDateTime.now());
    }
  }

  private void applyAutoHide(CommunityComment comment) {
    long distinctReporterCount =
        communityReportRepository.countDistinctReporterUser_IdByTargetTypeAndTargetId(
            ReportTargetType.COMMENT, comment.getId());
    if (distinctReporterCount >= REPORT_HIDE_THRESHOLD
        && comment.getVisibilityStatus() == VisibilityStatus.VISIBLE) {
      comment.hide(VisibilityReason.REPORT_THRESHOLD, LocalDateTime.now());
    }
  }

  private Map<Long, Boolean> loadLikedByPostId(Long viewerUserId, Collection<Long> postIds) {
    if (viewerUserId == null || postIds.isEmpty()) {
      return Map.of();
    }

    List<Long> likedPostIds = communityPostLikeRepository.findLikedPostIds(viewerUserId, postIds);
    Map<Long, Boolean> likedByPostId = new HashMap<>();
    for (Long likedPostId : likedPostIds) {
      likedByPostId.put(likedPostId, true);
    }
    return likedByPostId;
  }

  private List<CommunityPostImage> createImages(
      CommunityPost post, List<StoredCommunityImage> storedImages, LocalDateTime now) {
    if (storedImages == null || storedImages.isEmpty()) {
      return List.of();
    }

    List<CommunityPostImage> images = new ArrayList<>();
    for (StoredCommunityImage storedImage : storedImages) {
      images.add(
          CommunityPostImage.builder()
              .post(post)
              .imageUrl(storedImage.imageUrl())
              .sortOrder(storedImage.sortOrder())
              .contentType(storedImage.contentType())
              .fileSize(storedImage.fileSize())
              .width(storedImage.width())
              .height(storedImage.height())
              .createdAt(now)
              .build());
    }
    return images;
  }

  private static final int CONTENT_PREVIEW_MAX_LENGTH = 100;

  private CommunityPostSummaryItem toSummaryItem(CommunityPost post, boolean likedByMe) {
    String contentPreview = truncate(post.getContent(), CONTENT_PREVIEW_MAX_LENGTH);
    return new CommunityPostSummaryItem(
        post.getId(),
        post.getUser().getId(),
        post.getUser().getNickname(),
        post.getUser().getProfileImageUrl(),
        contentPreview,
        post.getRegion(),
        post.getSpecies(),
        post.getThumbnailImageUrl(),
        post.getLikeCount(),
        post.getCommentCount(),
        likedByMe,
        post.getCreatedAt());
  }

  private String truncate(String text, int maxLength) {
    if (text == null || text.length() <= maxLength) {
      return text;
    }
    return text.substring(0, maxLength) + "...";
  }

  private CommunityPostItem toPostItem(
      CommunityPost post, boolean likedByMe, List<CommunityPostImage> images) {
    return new CommunityPostItem(
        post.getId(),
        post.getUser().getId(),
        post.getUser().getNickname(),
        post.getUser().getProfileImageUrl(),
        post.getContent(),
        post.getRegion(),
        post.getPlaceName(),
        post.getLatitude(),
        post.getLongitude(),
        post.getFishedAt(),
        post.getSpecies(),
        post.getLengthCm(),
        post.getTackleType(),
        post.getTackleCustomText(),
        post.getThumbnailImageUrl() != null
            ? post.getThumbnailImageUrl()
            : firstSavedImageUrl(images),
        post.getLikeCount(),
        post.getCommentCount(),
        likedByMe,
        post.getCreatedAt());
  }

  private List<CommunityPostImageItem> toImageItems(List<CommunityPostImage> images) {
    return images.stream()
        .map(
            image ->
                new CommunityPostImageItem(
                    image.getId(),
                    image.getImageUrl(),
                    image.getSortOrder(),
                    image.getContentType(),
                    image.getFileSize(),
                    image.getWidth(),
                    image.getHeight()))
        .toList();
  }

  private CommunityCommentItem toCommentItem(CommunityComment comment) {
    boolean deleted = comment.getDeletedAt() != null;
    return new CommunityCommentItem(
        comment.getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getUser().getProfileImageUrl(),
        comment.getParentComment() == null ? null : comment.getParentComment().getId(),
        comment.getReplyToUser() == null ? null : comment.getReplyToUser().getId(),
        comment.getReplyToUser() == null ? null : comment.getReplyToUser().getNickname(),
        deleted ? null : comment.getContent(),
        comment.getLikeCount(),
        comment.getCreatedAt(),
        comment.getUpdatedAt(),
        deleted);
  }

  private CommunityPost getVisiblePost(Long postId) {
    return communityPostRepository
        .findByIdAndVisibilityStatus(postId, VisibilityStatus.VISIBLE)
        .orElseThrow(() -> new FishingException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
  }

  private CommunityPost getPostOrThrow(Long postId) {
    return communityPostRepository
        .findById(postId)
        .orElseThrow(() -> new FishingException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
  }

  private CommunityComment getCommentOrThrow(Long commentId) {
    return communityCommentRepository
        .findById(commentId)
        .orElseThrow(() -> new FishingException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
  }

  @Transactional
  public CommunityCommentItem editComment(Long commentId, Long userId, String content) {
    CommunityComment comment = getCommentOrThrow(commentId);
    if (!comment.getUser().getId().equals(userId)) {
      throw new FishingException(ErrorCode.COMMUNITY_COMMENT_FORBIDDEN);
    }
    if (comment.getDeletedAt() != null) {
      throw new FishingException(ErrorCode.COMMUNITY_COMMENT_DELETED);
    }
    comment.updateContent(content.trim(), LocalDateTime.now());
    return toCommentItem(comment);
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    CommunityComment comment = getCommentOrThrow(commentId);
    if (!comment.getUser().getId().equals(userId)) {
      throw new FishingException(ErrorCode.COMMUNITY_COMMENT_FORBIDDEN);
    }
    if (comment.getDeletedAt() != null) {
      throw new FishingException(ErrorCode.COMMUNITY_COMMENT_DELETED);
    }
    comment.softDelete(LocalDateTime.now());
  }

  @Transactional
  public CommunityPostDetailResponse editPost(
      Long postId,
      Long userId,
      UpdateCommunityPostRequest request,
      List<MultipartFile> newImageFiles) {
    CommunityPost post = getPostOrThrow(postId);
    if (!post.getUser().getId().equals(userId)) {
      throw new FishingException(ErrorCode.COMMUNITY_POST_FORBIDDEN);
    }
    if (post.getDeletedAt() != null) {
      throw new FishingException(ErrorCode.COMMUNITY_POST_DELETED);
    }

    LocalDateTime now = LocalDateTime.now();

    // 이미지 삭제
    if (request.deleteImageIds() != null && !request.deleteImageIds().isEmpty()) {
      communityPostImageRepository.deleteAllByPost_IdAndIdIn(postId, request.deleteImageIds());
    }

    // 새 이미지 업로드
    List<StoredCommunityImage> storedImages =
        imageStorageService.storeCommunityImages(newImageFiles);
    List<CommunityPostImage> newImages = createImages(post, storedImages, now);
    if (!newImages.isEmpty()) {
      communityPostImageRepository.saveAll(newImages);
    }

    // 남아있는 전체 이미지 조회 (삭제 후 + 새 이미지)
    List<CommunityPostImage> allImages =
        communityPostImageRepository.findAllByPost_IdOrderBySortOrderAsc(postId);

    // 썸네일 결정: 새 이미지가 있으면 첫 번째 새 이미지 썸네일, 없으면 기존 첫 이미지
    String thumbnailUrl = post.getThumbnailImageUrl();
    if (!storedImages.isEmpty()) {
      thumbnailUrl =
          storedImages.get(0).thumbnailUrl() != null
              ? storedImages.get(0).thumbnailUrl()
              : storedImages.get(0).imageUrl();
    } else if (!allImages.isEmpty()) {
      thumbnailUrl = allImages.get(0).getImageUrl();
    } else {
      thumbnailUrl = null;
    }

    post.updateAll(
        request.content().trim(),
        request.region().trim(),
        request.placeName(),
        toPoint(request.latitude(), request.longitude()),
        request.fishedAt(),
        request.fishedAtSource(),
        request.locationSource(),
        request.species(),
        request.lengthCm(),
        request.tackleType(),
        request.tackleCustomText(),
        thumbnailUrl,
        now);

    boolean likedByMe = communityPostLikeRepository.existsByPost_IdAndUser_Id(postId, userId);
    return new CommunityPostDetailResponse(
        toPostItem(post, likedByMe, allImages), toImageItems(allImages));
  }

  @Transactional
  public void deletePost(Long postId, Long userId) {
    CommunityPost post = getPostOrThrow(postId);
    if (!post.getUser().getId().equals(userId)) {
      throw new FishingException(ErrorCode.COMMUNITY_POST_FORBIDDEN);
    }
    if (post.getDeletedAt() != null) {
      throw new FishingException(ErrorCode.COMMUNITY_POST_DELETED);
    }
    post.softDelete(LocalDateTime.now());
  }

  @Transactional(readOnly = true)
  public List<MapPostItem> getAllMapPosts() {
    List<Object[]> rows = communityPostRepository.findAllVisibleWithLocation();
    return rows.stream()
        .map(
            row ->
                new MapPostItem(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2],
                    row[3] != null
                        ? ((String) row[3]).substring(0, Math.min(((String) row[3]).length(), 50))
                        : null,
                    ((Number) row[4]).doubleValue(),
                    ((Number) row[5]).doubleValue()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<CommunityRegionCountItem> getRegionCounts() {
    List<Object[]> rows = communityPostRepository.countByRegion(VisibilityStatus.VISIBLE);
    return rows.stream()
        .map(row -> new CommunityRegionCountItem((String) row[0], ((Number) row[1]).longValue()))
        .toList();
  }

  private User getUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));
  }

  private Point toPoint(Double latitude, Double longitude) {
    if (latitude == null && longitude == null) {
      return null;
    }
    if (latitude == null || longitude == null) {
      throw new FishingException(ErrorCode.INVALID_COORD);
    }

    Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    point.setSRID(4326);
    return point;
  }

  private String firstUploadedImageUrl(List<StoredCommunityImage> storedImages) {
    if (storedImages == null || storedImages.isEmpty()) {
      return null;
    }
    StoredCommunityImage first = storedImages.get(0);
    // 썸네일이 있으면 썸네일 URL을, 없으면 원본 URL을 사용
    return first.thumbnailUrl() != null ? first.thumbnailUrl() : first.imageUrl();
  }

  private String firstSavedImageUrl(List<CommunityPostImage> images) {
    if (images == null || images.isEmpty()) {
      return null;
    }
    return images.get(0).getImageUrl();
  }
}
