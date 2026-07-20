package dev.jihed.socialpub.persistence.entity;

import dev.jihed.socialpub.core.PublicationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "publication")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicationEntity {

  @Id private UUID id;

  @Column(columnDefinition = "text")
  private String caption;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private PublicationStatus status;

  @Column(name = "scheduled_at")
  private Instant scheduledAt;

  @Column(name = "overrides_json", columnDefinition = "text")
  private String overridesJson;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Builder.Default
  @OneToMany(
      mappedBy = "publication",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<PublicationMediaEntity> media = new ArrayList<>();

  @Builder.Default
  @OneToMany(
      mappedBy = "publication",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<PublicationTargetEntity> targets = new ArrayList<>();

  public void updateStatus(PublicationStatus newStatus) {
    this.status = newStatus;
    this.updatedAt = Instant.now();
  }

  public void addMedia(PublicationMediaEntity entity) {
    entity.attachTo(this);
    this.media.add(entity);
  }

  public void addTarget(PublicationTargetEntity entity) {
    entity.attachTo(this);
    this.targets.add(entity);
  }
}
