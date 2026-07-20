package dev.jihed.socialpub.persistence.entity;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.core.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "publication_target")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicationTargetEntity {

  @Id private UUID id;

  @ManyToOne
  @JoinColumn(name = "publication_id", nullable = false)
  private PublicationEntity publication;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Platform platform;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private PublicationStatus status;

  @Column(name = "external_post_id", length = 255)
  private String externalPostId;

  @Column(length = 1024)
  private String permalink;

  @Column(name = "error_code", length = 128)
  private String errorCode;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  void attachTo(PublicationEntity parent) {
    this.publication = parent;
  }

  public void markSuccess(String externalPostId, String permalink) {
    this.status = PublicationStatus.PUBLISHED;
    this.externalPostId = externalPostId;
    this.permalink = permalink;
    this.errorCode = null;
    this.errorMessage = null;
  }

  public void markFailure(String errorCode, String errorMessage) {
    this.status = PublicationStatus.FAILED;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }
}
