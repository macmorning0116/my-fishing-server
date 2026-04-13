package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityComment;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

  @Modifying
  @Query("UPDATE CommunityComment c SET c.reportCount = c.reportCount + 1 WHERE c.id = :commentId")
  void incrementReportCount(Long commentId);

  @Query(
      "SELECT c FROM CommunityComment c"
          + " JOIN FETCH c.user"
          + " LEFT JOIN FETCH c.parentComment"
          + " LEFT JOIN FETCH c.replyToUser"
          + " WHERE c.post.id = :postId"
          + " ORDER BY c.createdAt ASC")
  List<CommunityComment> findAllWithRelationsByPostId(@Param("postId") Long postId);

  @Query(
      "SELECT c FROM CommunityComment c"
          + " JOIN FETCH c.user"
          + " LEFT JOIN FETCH c.parentComment"
          + " LEFT JOIN FETCH c.replyToUser"
          + " WHERE c.post.id = :postId AND c.id > :cursor"
          + " ORDER BY c.createdAt ASC")
  List<CommunityComment> findAllWithRelationsByPostIdAndIdGreaterThan(
      @Param("postId") Long postId, @Param("cursor") Long cursor, Pageable pageable);

  @Query(
      "SELECT c FROM CommunityComment c"
          + " JOIN FETCH c.user"
          + " LEFT JOIN FETCH c.parentComment"
          + " LEFT JOIN FETCH c.replyToUser"
          + " WHERE c.post.id = :postId"
          + " ORDER BY c.createdAt ASC")
  List<CommunityComment> findFirstPageWithRelationsByPostId(
      @Param("postId") Long postId, Pageable pageable);

  List<CommunityComment> findAllByPost_IdAndVisibilityStatusOrderByCreatedAtAsc(
      Long postId, VisibilityStatus visibilityStatus);

  Optional<CommunityComment> findByIdAndPost_Id(Long commentId, Long postId);
}
