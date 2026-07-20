package dev.jihed.socialpub.core;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.core.port.PublicationStore;
import dev.jihed.socialpub.core.port.StagedPublication;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Minimal in-memory {@link PublicationStore} used to unit test the orchestrator. */
class InMemoryPublicationStore implements PublicationStore {

  record Row(PublishRequest request, Instant createdAt) {}

  final Map<UUID, Row> rows = new ConcurrentHashMap<>();
  final Map<UUID, PublicationStatus> statuses = new ConcurrentHashMap<>();
  final Map<UUID, Map<Platform, PlatformOutcome>> outcomes = new ConcurrentHashMap<>();
  final List<PublicationAttempt> attempts = Collections.synchronizedList(new ArrayList<>());

  @Override
  public UUID create(PublishRequest request, PublicationStatus initialStatus) {
    UUID id = UUID.randomUUID();
    rows.put(id, new Row(request, Instant.now()));
    statuses.put(id, initialStatus);
    outcomes.put(id, new ConcurrentHashMap<>());
    return id;
  }

  @Override
  public void updateStatus(UUID publicationId, PublicationStatus status) {
    statuses.put(publicationId, status);
  }

  @Override
  public void saveOutcome(UUID publicationId, Platform platform, PlatformOutcome outcome) {
    outcomes.computeIfAbsent(publicationId, k -> new ConcurrentHashMap<>()).put(platform, outcome);
  }

  @Override
  public void recordAttempt(PublicationAttempt attempt) {
    attempts.add(attempt);
  }

  @Override
  public Optional<PublicationView> get(UUID publicationId) {
    Row row = rows.get(publicationId);
    if (row == null) {
      return Optional.empty();
    }
    List<PublicationView.Target> targets = new ArrayList<>();
    Map<Platform, PlatformOutcome> outs = outcomes.getOrDefault(publicationId, Map.of());
    for (Platform platform : row.request().targets()) {
      PlatformOutcome outcome = outs.get(platform);
      targets.add(toTarget(platform, outcome));
    }
    return Optional.of(
        new PublicationView(
            publicationId,
            row.request().caption(),
            statuses.get(publicationId),
            row.request().scheduledAt(),
            row.createdAt(),
            targets));
  }

  private PublicationView.Target toTarget(Platform platform, PlatformOutcome outcome) {
    if (outcome instanceof PlatformOutcome.Success s) {
      return new PublicationView.Target(
          platform, PublicationStatus.PUBLISHED, s.postId(), s.permalink(), null, null);
    }
    if (outcome instanceof PlatformOutcome.Failure f) {
      return new PublicationView.Target(
          platform, PublicationStatus.FAILED, null, null, f.errorCode(), f.message());
    }
    return new PublicationView.Target(platform, PublicationStatus.PENDING, null, null, null, null);
  }

  @Override
  public List<PublicationView> list(
      PublicationStatus statusFilter, Platform platformFilter, int limit) {
    return rows.keySet().stream()
        .map(id -> get(id).orElseThrow())
        .filter(v -> statusFilter == null || v.status() == statusFilter)
        .filter(
            v ->
                platformFilter == null
                    || v.targets().stream().anyMatch(t -> t.platform() == platformFilter))
        .limit(limit)
        .toList();
  }

  @Override
  public List<StagedPublication> claimDue(Instant now, int max) {
    List<StagedPublication> due = new ArrayList<>();
    for (var entry : rows.entrySet()) {
      UUID id = entry.getKey();
      PublishRequest request = entry.getValue().request();
      if (statuses.get(id) == PublicationStatus.SCHEDULED
          && request.scheduledAt() != null
          && !request.scheduledAt().isAfter(now)) {
        statuses.put(id, PublicationStatus.PUBLISHING);
        due.add(new StagedPublication(id, request));
        if (due.size() >= max) {
          break;
        }
      }
    }
    return due;
  }

  @Override
  public boolean cancelScheduled(UUID publicationId) {
    if (statuses.get(publicationId) == PublicationStatus.SCHEDULED) {
      statuses.put(publicationId, PublicationStatus.CANCELLED);
      return true;
    }
    return false;
  }
}
