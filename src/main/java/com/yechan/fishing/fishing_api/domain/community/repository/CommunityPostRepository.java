package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPost;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

  @Modifying
  @Query("UPDATE CommunityPost p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
  void incrementLikeCount(Long postId);

  @Modifying
  @Query(
      "UPDATE CommunityPost p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1"
          + " ELSE 0 END WHERE p.id = :postId")
  void decrementLikeCount(Long postId);

  @Modifying
  @Query("UPDATE CommunityPost p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
  void incrementCommentCount(Long postId);

  @Modifying
  @Query(
      "UPDATE CommunityPost p SET p.commentCount = CASE WHEN p.commentCount > 0 THEN"
          + " p.commentCount - 1 ELSE 0 END WHERE p.id = :postId")
  void decrementCommentCount(Long postId);

  @Modifying
  @Query("UPDATE CommunityPost p SET p.reportCount = p.reportCount + 1 WHERE p.id = :postId")
  void incrementReportCount(Long postId);

  @Query(
      "SELECT p FROM CommunityPost p JOIN FETCH p.user"
          + " WHERE p.visibilityStatus = :status"
          + " ORDER BY p.id DESC")
  List<CommunityPost> findAllWithUserByVisibilityStatus(
      @Param("status") VisibilityStatus status, Pageable pageable);

  @Query(
      "SELECT p FROM CommunityPost p JOIN FETCH p.user"
          + " WHERE p.visibilityStatus = :status AND p.id < :cursor"
          + " ORDER BY p.id DESC")
  List<CommunityPost> findAllWithUserByVisibilityStatusAndIdLessThan(
      @Param("status") VisibilityStatus status, @Param("cursor") Long cursor, Pageable pageable);

  @Query(
      "SELECT p FROM CommunityPost p JOIN FETCH p.user"
          + " WHERE p.user.id = :userId AND p.visibilityStatus = :status"
          + " ORDER BY p.id DESC")
  List<CommunityPost> findAllWithUserByUserIdAndVisibilityStatus(
      @Param("userId") Long userId, @Param("status") VisibilityStatus status, Pageable pageable);

  @Query(
      "SELECT p FROM CommunityPost p JOIN FETCH p.user"
          + " WHERE p.user.id = :userId AND p.visibilityStatus = :status AND p.id < :cursor"
          + " ORDER BY p.id DESC")
  List<CommunityPost> findAllWithUserByUserIdAndVisibilityStatusAndIdLessThan(
      @Param("userId") Long userId,
      @Param("status") VisibilityStatus status,
      @Param("cursor") Long cursor,
      Pageable pageable);

  Optional<CommunityPost> findByIdAndVisibilityStatus(
      Long postId, VisibilityStatus visibilityStatus);

  long countByUser_IdAndVisibilityStatus(Long userId, VisibilityStatus visibilityStatus);

  @Query(
      value =
          "SELECT p.id, p.thumbnail_image_url, p.species, p.content, "
              + "ST_Y(p.location::geometry) as latitude, ST_X(p.location::geometry) as longitude "
              + "FROM community_posts p "
              + "WHERE p.visibility_status = 'VISIBLE' "
              + "AND p.location IS NOT NULL "
              + "ORDER BY p.id DESC",
      nativeQuery = true)
  List<Object[]> findAllVisibleWithLocation();

  @Query(
      "SELECT p.region, COUNT(p) FROM CommunityPost p WHERE p.visibilityStatus"
          + " = :status AND p.region IS NOT NULL GROUP BY p.region")
  List<Object[]> countByRegion(@Param("status") VisibilityStatus status);
}
