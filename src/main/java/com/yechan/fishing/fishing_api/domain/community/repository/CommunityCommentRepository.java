package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityComment;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

  @Modifying
  @Query("UPDATE CommunityComment c SET c.reportCount = c.reportCount + 1 WHERE c.id = :commentId")
  void incrementReportCount(Long commentId);

  List<CommunityComment> findAllByPost_IdAndVisibilityStatusOrderByCreatedAtAsc(
      Long postId, VisibilityStatus visibilityStatus);

  Optional<CommunityComment> findByIdAndPost_Id(Long commentId, Long postId);
}
