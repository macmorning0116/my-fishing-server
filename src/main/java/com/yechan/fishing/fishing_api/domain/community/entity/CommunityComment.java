package com.yechan.fishing.fishing_api.domain.community.entity;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityReason;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "community_comments")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private CommunityPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_comment_id")
  private CommunityComment parentComment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reply_to_user_id")
  private User replyToUser;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "like_count", nullable = false)
  private Integer likeCount = 0;

  @Column(name = "report_count", nullable = false)
  private Integer reportCount = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility_status", nullable = false, length = 20)
  private VisibilityStatus visibilityStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility_reason", length = 30)
  private VisibilityReason visibilityReason;

  @Column(name = "hidden_at")
  private LocalDateTime hiddenAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void incrementReportCount() {
    reportCount = reportCount + 1;
  }

  public void updateContent(String content, LocalDateTime now) {
    this.content = content;
    this.updatedAt = now;
  }

  public void softDelete(LocalDateTime now) {
    this.deletedAt = now;
    this.updatedAt = now;
  }

  public void hide(VisibilityReason reason, LocalDateTime now) {
    visibilityStatus = VisibilityStatus.HIDDEN;
    visibilityReason = reason;
    hiddenAt = now;
  }
}
