package dev.jihed.socialpub.persistence.entity;

import dev.jihed.socialpub.api.MediaType;
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
@Table(name = "publication_media")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicationMediaEntity {

  @Id private UUID id;

  @ManyToOne
  @JoinColumn(name = "publication_id", nullable = false)
  private PublicationEntity publication;

  @Column(name = "source_url", columnDefinition = "text")
  private String sourceUrl;

  @Column(name = "object_key", columnDefinition = "text")
  private String objectKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "media_type", length = 16)
  private MediaType mediaType;

  @Column(length = 128)
  private String mime;

  @Column(length = 64)
  private String sha256;

  @Column(name = "size_bytes")
  private Long sizeBytes;

  @Column(name = "duration_ms")
  private Long durationMs;

  void attachTo(PublicationEntity parent) {
    this.publication = parent;
  }
}
