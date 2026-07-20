package dev.jihed.socialpub.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "publication_attempt")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicationAttemptEntity {

  @Id private UUID id;

  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Column(name = "attempt_no", nullable = false)
  private int attemptNo;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "http_status")
  private Integer httpStatus;

  @Column(name = "response_excerpt", columnDefinition = "text")
  private String responseExcerpt;
}
