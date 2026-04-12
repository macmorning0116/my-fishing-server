package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPost;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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

  List<CommunityPost> findAllByVisibilityStatusOrderByIdDesc(
      VisibilityStatus visibilityStatus, Pageable pageable);

  List<CommunityPost> findAllByVisibilityStatusAndIdLessThanOrderByIdDesc(
      VisibilityStatus visibilityStatus, Long cursor, Pageable pageable);

  Optional<CommunityPost> findByIdAndVisibilityStatus(
      Long postId, VisibilityStatus visibilityStatus);

  List<CommunityPost> findAllByUser_IdAndVisibilityStatusOrderByIdDesc(
      Long userId, VisibilityStatus visibilityStatus, Pageable pageable);

  List<CommunityPost> findAllByUser_IdAndVisibilityStatusAndIdLessThanOrderByIdDesc(
      Long userId, VisibilityStatus visibilityStatus, Long cursor, Pageable pageable);

  long countByUser_IdAndVisibilityStatus(Long userId, VisibilityStatus visibilityStatus);
}
