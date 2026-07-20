package dev.jihed.socialpub.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jihed.socialpub.api.MediaRef;
import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.api.PlatformOverrides;
import dev.jihed.socialpub.core.PublicationAttempt;
import dev.jihed.socialpub.core.PublicationStatus;
import dev.jihed.socialpub.core.PublicationView;
import dev.jihed.socialpub.core.PublishRequest;
import dev.jihed.socialpub.core.port.PublicationStore;
import dev.jihed.socialpub.core.port.StagedPublication;
import dev.jihed.socialpub.persistence.entity.PublicationAttemptEntity;
import dev.jihed.socialpub.persistence.entity.PublicationEntity;
import dev.jihed.socialpub.persistence.entity.PublicationMediaEntity;
import dev.jihed.socialpub.persistence.entity.PublicationTargetEntity;
import dev.jihed.socialpub.persistence.repo.PublicationAttemptRepository;
import dev.jihed.socialpub.persistence.repo.PublicationRepository;
import dev.jihed.socialpub.persistence.repo.PublicationTargetRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA-backed implementation of the {@link PublicationStore} port. */
@Component
public class JpaPublicationStore implements PublicationStore {

  private static final Logger log = LoggerFactory.getLogger(JpaPublicationStore.class);

  private final PublicationRepository publications;
  private final PublicationTargetRepository targets;
  private final PublicationAttemptRepository attempts;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public JpaPublicationStore(
      PublicationRepository publications,
      PublicationTargetRepository targets,
      PublicationAttemptRepository attempts) {
    this.publications = publications;
    this.targets = targets;
    this.attempts = attempts;
  }

  @Override
  @Transactional
  public UUID create(PublishRequest request, PublicationStatus initialStatus) {
    Instant now = Instant.now();
    UUID id = UUID.randomUUID();
    PublicationEntity entity =
        PublicationEntity.builder()
            .id(id)
            .caption(request.caption())
            .status(initialStatus)
            .scheduledAt(request.scheduledAt())
            .overridesJson(serializeOverrides(request.overrides()))
            .createdAt(now)
            .updatedAt(now)
            .build();

    for (MediaRef ref : request.media()) {
      entity.addMedia(
          PublicationMediaEntity.builder()
              .id(UUID.randomUUID())
              .sourceUrl(ref.url())
              .objectKey(ref.objectKey())
              .mediaType(ref.type())
              .mime(ref.mime())
              .sizeBytes(ref.sizeBytes())
              .durationMs(ref.durationMs())
              .build());
    }
    for (Platform platform : request.targets()) {
      entity.addTarget(
          PublicationTargetEntity.builder()
              .id(UUID.randomUUID())
              .platform(platform)
              .status(PublicationStatus.PENDING)
              .build());
    }
    publications.save(entity);
    return id;
  }

  @Override
  @Transactional
  public void updateStatus(UUID publicationId, PublicationStatus status) {
    publications
        .findById(publicationId)
        .ifPresent(entity -> entity.updateStatus(status)); // dirty-checked on flush
  }

  @Override
  @Transactional
  public void saveOutcome(UUID publicationId, Platform platform, PlatformOutcome outcome) {
    Optional<PublicationTargetEntity> target =
        targets.findByPublicationIdAndPlatform(publicationId, platform);
    if (target.isEmpty()) {
      log.warn("No target row for {}/{}, outcome dropped", publicationId, platform);
      return;
    }
    PublicationTargetEntity entity = target.get();
    if (outcome instanceof PlatformOutcome.Success s) {
      entity.markSuccess(s.postId(), s.permalink());
    } else {
      PlatformOutcome.Failure f = (PlatformOutcome.Failure) outcome;
      entity.markFailure(f.errorCode(), f.message());
    }
  }

  @Override
  @Transactional
  public void recordAttempt(PublicationAttempt attempt) {
    UUID targetId =
        targets
            .findByPublicationIdAndPlatform(attempt.publicationId(), attempt.platform())
            .map(PublicationTargetEntity::getId)
            .orElse(null);
    if (targetId == null) {
      return;
    }
    attempts.save(
        PublicationAttemptEntity.builder()
            .id(UUID.randomUUID())
            .targetId(targetId)
            .attemptNo(attempt.attemptNo())
            .startedAt(attempt.startedAt())
            .finishedAt(attempt.finishedAt())
            .httpStatus(attempt.httpStatus())
            .responseExcerpt(attempt.responseExcerpt())
            .build());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PublicationView> get(UUID publicationId) {
    return publications.findById(publicationId).map(this::toView);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PublicationView> list(
      PublicationStatus statusFilter, Platform platformFilter, int limit) {
    List<PublicationEntity> all =
        statusFilter != null
            ? publications.findByStatusOrderByCreatedAtDesc(statusFilter)
            : publications.findAll();
    return all.stream()
        .filter(
            e ->
                platformFilter == null
                    || e.getTargets().stream().anyMatch(t -> t.getPlatform() == platformFilter))
        .limit(limit)
        .map(this::toView)
        .toList();
  }

  @Override
  @Transactional
  public List<StagedPublication> claimDue(Instant now, int max) {
    List<StagedPublication> claimed = new ArrayList<>();
    for (PublicationEntity entity : publications.lockDue(now, max)) {
      entity.updateStatus(PublicationStatus.PUBLISHING);
      claimed.add(new StagedPublication(entity.getId(), toRequest(entity)));
    }
    return claimed;
  }

  @Override
  @Transactional
  public boolean cancelScheduled(UUID publicationId) {
    Optional<PublicationEntity> entity = publications.findById(publicationId);
    if (entity.isPresent() && entity.get().getStatus() == PublicationStatus.SCHEDULED) {
      entity.get().updateStatus(PublicationStatus.CANCELLED);
      return true;
    }
    return false;
  }

  private PublicationView toView(PublicationEntity entity) {
    List<PublicationView.Target> targetViews =
        entity.getTargets().stream()
            .map(
                t ->
                    new PublicationView.Target(
                        t.getPlatform(),
                        t.getStatus(),
                        t.getExternalPostId(),
                        t.getPermalink(),
                        t.getErrorCode(),
                        t.getErrorMessage()))
            .toList();
    return new PublicationView(
        entity.getId(),
        entity.getCaption(),
        entity.getStatus(),
        entity.getScheduledAt(),
        entity.getCreatedAt(),
        targetViews);
  }

  private PublishRequest toRequest(PublicationEntity entity) {
    List<MediaRef> media =
        entity.getMedia().stream()
            .map(
                m ->
                    new MediaRef(
                        m.getSourceUrl(),
                        m.getObjectKey(),
                        m.getMediaType(),
                        m.getMime(),
                        m.getSizeBytes(),
                        m.getDurationMs()))
            .toList();
    var targetPlatforms =
        entity.getTargets().stream()
            .map(PublicationTargetEntity::getPlatform)
            .collect(java.util.stream.Collectors.toSet());
    return new PublishRequest(
        entity.getCaption(),
        media,
        targetPlatforms,
        entity.getScheduledAt(),
        deserializeOverrides(entity.getOverridesJson()));
  }

  private String serializeOverrides(Map<Platform, PlatformOverrides> overrides) {
    if (overrides == null || overrides.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(overrides);
    } catch (Exception e) {
      log.warn("Failed to serialize overrides, dropping them", e);
      return null;
    }
  }

  private Map<Platform, PlatformOverrides> deserializeOverrides(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(
          json, new TypeReference<LinkedHashMap<Platform, PlatformOverrides>>() {});
    } catch (Exception e) {
      log.warn("Failed to read overrides json, ignoring", e);
      return Map.of();
    }
  }
}
