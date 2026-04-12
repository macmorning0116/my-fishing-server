package com.yechan.fishing.fishing_api.domain.community.entity;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.FishedAtSource;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.LocationSource;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.TackleType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Getter
@Entity
@Table(name = "community_posts")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false, length = 50)
  private String region;

  @Column(name = "place_name", length = 255)
  private String placeName;

  @JdbcTypeCode(SqlTypes.GEOGRAPHY)
  @Column(name = "location", columnDefinition = "geography(Point,4326)")
  private Point location;

  @Column(name = "fished_at")
  private LocalDateTime fishedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "fished_at_source", length = 20)
  private FishedAtSource fishedAtSource;

  @Enumerated(EnumType.STRING)
  @Column(name = "location_source", length = 20)
  private LocationSource locationSource;

  @Column(length = 100)
  private String species;

  @Column(name = "length_cm")
  private Integer lengthCm;

  @Enumerated(EnumType.STRING)
  @Column(name = "tackle_type", length = 50)
  private TackleType tackleType;

  @Column(name = "tackle_custom_text", length = 255)
  private String tackleCustomText;

  @Column(name = "thumbnail_image_url", columnDefinition = "TEXT")
  private String thumbnailImageUrl;

  @Column(name = "like_count", nullable = false)
  private Integer likeCount = 0;

  @Column(name = "comment_count", nullable = false)
  private Integer commentCount = 0;

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

  public Double getLatitude() {
    return location == null ? null : location.getY();
  }

  public Double getLongitude() {
    return location == null ? null : location.getX();
  }

  public void incrementLikeCount() {
    likeCount = likeCount + 1;
  }

  public void decrementLikeCount() {
    likeCount = Math.max(0, likeCount - 1);
  }

  public void incrementCommentCount() {
    commentCount = commentCount + 1;
  }

  public void decrementCommentCount() {
    commentCount = Math.max(0, commentCount - 1);
  }

  public void incrementReportCount() {
    reportCount = reportCount + 1;
  }

  public void hide(VisibilityReason reason, LocalDateTime now) {
    visibilityStatus = VisibilityStatus.HIDDEN;
    visibilityReason = reason;
    hiddenAt = now;
  }
}
