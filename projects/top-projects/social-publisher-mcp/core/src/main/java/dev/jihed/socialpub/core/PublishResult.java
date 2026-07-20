package dev.jihed.socialpub.core;

import dev.jihed.socialpub.api.Platform;
import dev.jihed.socialpub.api.PlatformOutcome;
import java.util.Map;
import java.util.UUID;

public record PublishResult(UUID publicationId, Map<Platform, PlatformOutcome> outcomes) {

  public PublishResult {
    outcomes = outcomes == null ? Map.of() : Map.copyOf(outcomes);
  }

  /** Roll the per-platform outcomes up into a single publication status. */
  public PublicationStatus overallStatus() {
    if (outcomes.isEmpty()) {
      return PublicationStatus.PENDING;
    }
    long ok = outcomes.values().stream().filter(PlatformOutcome::success).count();
    if (ok == outcomes.size()) {
      return PublicationStatus.PUBLISHED;
    }
    if (ok == 0) {
      return PublicationStatus.FAILED;
    }
    return PublicationStatus.PARTIALLY_PUBLISHED;
  }
}
