package dev.jihed.socialpub.core.port;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import dev.jihed.socialpub.core.PublicationAttempt;
import dev.jihed.socialpub.core.PublicationStatus;
import dev.jihed.socialpub.core.PublicationView;
import dev.jihed.socialpub.core.PublishRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence boundary for publications, their per-platform targets and the attempt audit trail.
 * Implemented by the {@code persistence} module; the orchestrator only knows this port.
 */
public interface PublicationStore {

  /** Persist a new publication (with its staged media and targets) and return its id. */
  UUID create(PublishRequest request, PublicationStatus initialStatus);

  void updateStatus(UUID publicationId, PublicationStatus status);

  /** Record the final outcome for one platform target. */
  void saveOutcome(UUID publicationId, Platform platform, PlatformOutcome outcome);

  void recordAttempt(PublicationAttempt attempt);

  Optional<PublicationView> get(UUID publicationId);

  List<PublicationView> list(PublicationStatus statusFilter, Platform platformFilter, int limit);

  /**
   * Atomically claim publications whose {@code scheduledAt} is due, flipping them to {@code
   * PUBLISHING} so a second poller can't pick them up (FOR UPDATE SKIP LOCKED).
   */
  List<StagedPublication> claimDue(Instant now, int max);

  /** Cancel a scheduled publication. Returns false if it was not in {@code SCHEDULED}. */
  boolean cancelScheduled(UUID publicationId);
}
