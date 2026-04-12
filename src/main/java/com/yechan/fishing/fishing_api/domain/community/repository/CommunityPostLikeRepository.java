package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPostLike;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

  boolean existsByPost_IdAndUser_Id(Long postId, Long userId);

  long countByPost_Id(Long postId);

  Optional<CommunityPostLike> findByPost_IdAndUser_Id(Long postId, Long userId);

  @Query(
      "select like.post.id from CommunityPostLike like "
          + "where like.user.id = :userId and like.post.id in :postIds")
  List<Long> findLikedPostIds(
      @Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}
